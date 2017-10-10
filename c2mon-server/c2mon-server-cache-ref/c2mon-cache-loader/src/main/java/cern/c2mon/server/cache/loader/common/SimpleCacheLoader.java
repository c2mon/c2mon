package cern.c2mon.server.cache.loader.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.server.cache.loader.CacheLoaderDAO;
import cern.c2mon.shared.common.Cacheable;

/**
 * Cache loader that loads the Ehcache underlying any C2monCache. It contains
 * the common logic for loading the cache from the database. It must be
 * provided with a reference to the Ehcache that needs loading and the DAO
 * that contains the required methods for fetching the objects from the DB.
 * <p>
 * <p>One of these classes is instantiated for every cache used by the
 * server (done in Spring XML file).
 * <p>
 * <p>Uses the Ehcache CacheLoader interface.
 * <p>
 * <p>TODO loading threads are hardcoded here; may wish to move these
 * to parameters, but better is to use BatchCacheLoader instead
 *
 * @param <T> the cache object type
 *
 * @author Mark Brightwell
 * @author Szymon Halastra
 * @deprecated use {@link BatchCacheLoader} instead if starting from scratch
 * as better performance for large caches
 */
@Slf4j
public class SimpleCacheLoader<T extends Cacheable> implements CacheLoader {

  /**
   * The map used to load objects into the cache from the DB at startup.
   * Is loaded (by iBatis) and then accessed by loading mechanism - no
   * synchronization necessary.
   */
  private Map<Long, T> preloadBuffer = new ConcurrentHashMap<>();

  private final Cache cache;

  private final CacheLoaderDAO<T> cacheLoaderDAO;

  /**
   * Constructor (used in Spring XML to instantiate the loaders
   * for the different caches).
   *
   * @param cache            the cache to load from the DB
   * @param cacheLoaderDAO   the DAO for accessing the DB
   * @param executor
   * @param threadNamePrefix
   */
  public SimpleCacheLoader(Cache cache, CacheLoaderDAO<T> cacheLoaderDAO) {
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
    // fill buffer from DB (uses iBatis call)
    preloadBuffer = fillBufferFromDB();

    //load the cache from the buffer
    if (preloadBuffer != null) {
      log.debug("Loading the cache from the buffer...");
      loadCacheFromBuffer(preloadBuffer);
      log.debug("\t...done");
    }
    else {
      log.error("Attempt to call loadCacheFromBuffer with null buffer: "
              + "this should not happen and needs investigating!");
    }
    //loading is done on one node only; if the design is switched to multiple nodes, then need to wait for
    //all nodes to be coherent here - NOW DONE IN SUPERVISION MANAGER by waiting for all nodes at that point
  }

  /**
   * Loads all the values in the provided Map into the cache, using multiple threads.
   *
   * @param preloadBuffer the Map key -> object to load into the cache
   */
  protected void loadCacheFromBuffer(final Map<Long, T> preloadBuffer) {
    //set the local Ehcache node to incoherent, which speeds up the loading process in the Terracotta setup
    //(when in single server mode, will throw an exception which we catch and log)
    loadCache(preloadBuffer.keySet());

  }

  private void loadCache(Collection<Long> keySet) {
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 16, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));
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
    }
    catch (InterruptedException e) {
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
   */
  private class CacheLoaderTask implements Runnable {

    /**
     * List of keys of objects to load.
     */
    private LinkedList<Object> keyList = new LinkedList<>();

    /**
     * Add a key to the list.
     *
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
        cache.put(key, preloadBuffer.get(key));
      }
    }
  }
}
