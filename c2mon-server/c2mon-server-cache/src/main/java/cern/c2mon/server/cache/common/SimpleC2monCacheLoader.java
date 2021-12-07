/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.cache.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cern.c2mon.server.cache.loading.common.BatchCacheLoader;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;

import lombok.extern.slf4j.Slf4j;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.loading.CacheLoaderDAO;
import cern.c2mon.shared.common.Cacheable;

/**
 * Cache loader that loads the Ehcache underlying any C2monCache. It contains
 * the common logic for loading the cache from the database. It must be
 * provided with a reference to the Ehcache that needs loading and the DAO
 * that contains the required methods for fetching the objects from the DB.
 *
 * <p>One of these classes is instantiated for every cache used by the
 * server (done in Spring XML file).
 *
 * <p>Uses the Ehcache CacheLoader interface.
 *
 * <p>TODO loading threads are hardcoded here; may wish to move these
 * to parameters, but better is to use BatchCacheLoader instead
 *
 * @param <T> the cache object type
 *
 * @author Mark Brightwell
 * @deprecated use {@link BatchCacheLoader} instead if starting from scratch
 *              as better performance for large caches
 */
@Slf4j
public class SimpleC2monCacheLoader<T extends Cacheable> implements C2monCacheLoader {

  /**
   * Reference to the distributed parameters (used to lock server start up).
   * Use field autowiring to avoid use in all XML instantiation.
   */
  @Autowired
  private ClusterCache clusterCache;

  /**
   * The map used to load objects into the cache from the DB at startup.
   * Is loaded (by iBatis) and then accessed by loading mechanism - no
   * synchronization necessary.
   */
  private Map<Long, T> preloadBuffer = new ConcurrentHashMap<Long, T>();

  /**
   * Reference to the cache that needs loading
   * (set in the constructor).
   */
  private Ehcache cache;

  /**
   * Reference to the loader DAO for this cache
   * (set in constructor).
   */
  private CacheLoaderDAO<T> cacheLoaderDAO;


  /**
   * Constructor (used in Spring XML to instantiate the loaders
   * for the different caches).
   *
   * @param cache the cache to load from the DB
   * @param cacheLoaderDAO the DAO for accessing the DB
   */
  public SimpleC2monCacheLoader(final Ehcache cache, final CacheLoaderDAO<T> cacheLoaderDAO) {
    this.cache = cache;
    this.cacheLoaderDAO = cacheLoaderDAO;
  }

  /**
   * Preload the cache from the database. First loads the objects from the DB
   * into a map (on single threads so far) and then loads the cache from the
   * map (on multiple threads).
   */
  @Override
  public void preload() {

    //acquire read lock on start-up - this prevents the server from starting
    //until all caches are loaded (many can acquire read lock).
    clusterCache.acquireReadLockOnKey(aliveStatusInitialized);
    try {

      //fill buffer from DB (uses iBatis call)
      preloadBuffer = fillBufferFromDB();

      //load the cache from the buffer
      if (preloadBuffer != null) {
        log.debug("Loading the cache from the buffer...");
        loadCacheFromBuffer(preloadBuffer);
        log.debug("\t...done");
      } else {
        log.error("Attempt to call loadCacheFromBuffer with null buffer: "
            + "this should not happen and needs investigating!");
      }
      //loading is done on one node only; if the design is switched to multiple nodes, then need to wait for
      //all nodes to be coherent here - NOW DONE IN SUPERVISION MANAGER by waiting for all nodes at that point

    } finally {
      clusterCache.releaseReadLockOnKey(aliveStatusInitialized);
    }
  }

  /**
   * Loads all the values in the provided Map into the cache, using multiple threads.
   * @param preloadBuffer the Map key -> object to load into the cache
   */
  protected void loadCacheFromBuffer(final Map<Long, T> preloadBuffer) {
    //set the local Ehcache node to incoherent, which speeds up the loading process in the Terracotta setup
    //(when in single server mode, will throw an exception which we catch and log)
    loadCache(preloadBuffer.keySet());
  }

  /**
   * Loads the cache objects from the TC server into the node memory.
   */
  public void loadNode() {
    try {
      cache.setNodeBulkLoadEnabled(true);
    } catch (UnsupportedOperationException ex) {
      log.warn("setNodeBulkLoadEnabled() method threw an exception when "
          + "loading the cache (UnsupportedOperationException) - this is "
          + "normal behaviour in a single-server mode and can be ignored");
    }
    loadCache(cache.getKeys());
    try {
      cache.setNodeBulkLoadEnabled(false);
    } catch (UnsupportedOperationException ex) {
      log.warn("setNodeBulkLoadEnabled() method threw an exception when "
          + "loading the cache (UnsupportedOperationException) - this is "
          + "normal behaviour in a single-server mode and can be ignored");
    }
  }

  private void loadCache(Collection<Long> keySet) {

    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 16, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000));
    Iterator<Long> it = keySet.iterator();

    CacheLoaderTask loaderTask = null;
    int counter = 0;
    boolean executeNecessary = false;
    while (it.hasNext()) {
      if (counter == 0) {
        loaderTask = new CacheLoaderTask();
        executeNecessary = true;
      }
      loaderTask.addToList(it.next());
      counter++;
      if (counter == 500) {              //execute in batch of 500
        threadPoolExecutor.execute(loaderTask);
        executeNecessary = false;
        counter = 0;
      }
    }
    if (executeNecessary) {
      threadPoolExecutor.execute(loaderTask);
    }
    threadPoolExecutor.shutdown();
    try {
      threadPoolExecutor.awaitTermination(1200, TimeUnit.SECONDS); //TODO move to config?constant?
    } catch (InterruptedException e) {
      log.warn("Exception caught while waiting for cache loading threads to complete (waited longer then timeout?): ", e);
    }
  }

  private Map<Long, T> fillBufferFromDB() {
    return cacheLoaderDAO.getAllAsMap();
  }

  /**
   * Task of loading a list of objects into the cache.
   *
   * @author Mark Brightwell
   *
   */
  private class CacheLoaderTask implements Runnable {

    /**
     * List of keys of objects to load.
     */
    private LinkedList<Object> keyList = new LinkedList<Object>();

    /**
     * Add a key to the list.
     * @param key the Id to add (Long)
     */
    public void addToList(final Object key) {
      keyList.offer(key);
    }

    /**
     * Loads the list of objects into the cache (single threaded here).
     * Uses <code>getWithLoader()</code> Ehcache method rather than
     * <code>load()</code> method since the latter starts a new thread.
     */
    @Override
    public void run() {
      while (!keyList.isEmpty()) {
          Object key = keyList.pollFirst();
          cache.putQuiet(key, preloadBuffer.get(key));
          cache.putQuiet(key, preloadBuffer.get(key));
      }
    }
  }
}
