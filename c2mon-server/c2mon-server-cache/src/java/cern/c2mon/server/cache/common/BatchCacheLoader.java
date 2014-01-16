/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2014 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.cache.common;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import cern.c2mon.server.cache.loading.BatchCacheLoaderDAO;
import cern.c2mon.shared.common.Cacheable;

/**
 * Cache loader implementation that loads the cache on multiple threads. The cache
 * must provided a BatchCacheLoaderDAO implementation.
 * 
 * @param <T> the cache object type
 * 
 * @author Mark Brightwell
 *
 */
public class BatchCacheLoader<T extends Cacheable> implements C2monCacheLoader {

  /**
   * Private logger.
   */
  private static final Logger LOGGER = Logger.getLogger(BatchCacheLoader.class);
  
  /**
   * Executor for loading the cache using multiple threads.
   */
  private ThreadPoolExecutor mapLoaderExecutor;  
    
  /**
   * Timeout before an inactive thread is returned to the pool
   */
  private static final int THREAD_TIMEOUT = 5; //in seconds
  
  /**
   * The max number of cache object fetched in one query from the DB and loaded
   * into the cache in a single thread (covers this many ids, but all need not
   * be used).
   */
  private final int batchSize;
  
  /**
   * The size of the queue containing the waiting tasks
   * (should be large enough to contain all the tasks as
   * the executor will throw an exception if a task is
   * rejected).
   */
  private final int taskQueueSize;
  
  /**
   * The number of threads used to load the cache.
   */
  private final int loaderThreads;
  
  /**
   * Cache
   */
  private final Ehcache cache;
  
  /**
   * Reference to batch loader DAO
   */
  private final BatchCacheLoaderDAO<T> batchCacheLoaderDAO;
  
  /**
   * Constructor (used in Spring XML to instantiate the loaders
   * for the different caches).
   * 
   * @param cache the cache to load from the DB
   * @param cacheLoaderDAO the DAO for accessing the DB
   * @param batchSize the number of object loaded in a single task
   * @param loaderThreads the number of threads processing the loading tasks (fixed)
   * @param taskQueueSize the size of the executor's queue
   */
  public BatchCacheLoader(final Ehcache cache, 
                          final BatchCacheLoaderDAO<T> cacheLoaderDAO, 
                          final int batchSize,
                          final int loaderThreads, 
                          final int taskQueueSize) {     
    this.batchSize = batchSize;
    this.loaderThreads = loaderThreads;
    this.taskQueueSize = taskQueueSize;
    this.batchCacheLoaderDAO = cacheLoaderDAO;
    this.cache = cache;
  }
  
  @Override
  public void preload() {
    LOGGER.debug("preload() - Start preloading data for cache " + cache.getName());
    Long maxId = batchCacheLoaderDAO.getMaxId(); // 0 if no cache objects!
    mapLoaderExecutor = new ThreadPoolExecutor(loaderThreads, loaderThreads, 
        THREAD_TIMEOUT, TimeUnit.SECONDS, 
        new ArrayBlockingQueue<Runnable>(taskQueueSize));
    Long firstId = batchCacheLoaderDAO.getMinId();
    LinkedList<Callable<Object>> tasks = new LinkedList<Callable<Object>>();
    while (firstId <= maxId) {
      MapLoaderTask mapTask = new MapLoaderTask(firstId , firstId + batchSize - 1);
      tasks.push(mapTask);        
      firstId = firstId + batchSize;
    }
    try {
      mapLoaderExecutor.invokeAll(tasks, 1800, TimeUnit.SECONDS);
    } catch (RejectedExecutionException e) {
      LOGGER.fatal("Exception caught while loading a server cache from the database. This is probably due to the cache.loader.queue.size being"
          + "too small. Increase this to at least 'id range'/'cache loader batch size', or alternatively increase the"
          + "batch size.");
      throw e;
    } catch (InterruptedException e) {
      LOGGER.error("Interrupted while waiting for cache loading threads to terminate.", e);      
    }       
    mapLoaderExecutor.shutdown();
    LOGGER.debug("preload() - Finished preload for cache " + cache.getName());
  }
  
  /**
   * Task that loads a batch of cache objects into
   * the cache. A batch must be specified by a first
   * and last Cacheable id and the DAO must implement the
   * <code>getBatchAsMap</code> method.
   * 
   * <p>Returns null on successful completion.
   * 
   * @author Mark Brightwell
   *
   */
  private class MapLoaderTask implements Callable<Object> {

    long firstId;
    long lastId;
    
    public MapLoaderTask(long startRow, long endRow) {
     this.firstId = startRow;
     this.lastId = endRow;
    }
    
    @Override
    public Object call() {
      Map<Object, T> cacheLoaderMap = batchCacheLoaderDAO.getBatchAsMap(firstId, lastId);
      //preloadBuffer.putAll(cacheLoaderMap);
      for (Object key : cacheLoaderMap.keySet()) {
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("MapLoaderTask - Putting key " + key + " to cache " + cache.getName());
        }
        cache.putQuiet(new Element(key, cacheLoaderMap.get(key)));
        //Object cacheObject = cache.get(key);
      }
      return null;
    }
    
  }

}
