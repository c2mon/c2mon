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
package cern.c2mon.server.cache.loading.common;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.*;

import cern.c2mon.server.cache.loading.BatchCacheLoaderDAO;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Cache loader implementation that loads the cache on multiple threads. The cache
 * must provided a BatchCacheLoaderDAO implementation.
 *
 * @param <T> the cache object type
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
public class BatchCacheLoader<T extends Cacheable> implements C2monCacheLoader {

  /**
   * Executor for loading the cache using multiple threads.
   */
  @Autowired
  private ThreadPoolTaskExecutor cacheThreadPoolTaskExecutor;

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
   * Name prefix for ThreadPool threads
   */
  private final String threadNamePrefix;
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
   * @param threadNamePrefix the name of thread pool
   */
  public BatchCacheLoader(final Ehcache cache,
                          final BatchCacheLoaderDAO<T> cacheLoaderDAO,
                          final int batchSize,
                          final String threadNamePrefix) {
    this.batchSize = batchSize;
    this.batchCacheLoaderDAO = cacheLoaderDAO;
    this.cache = cache;
    this.threadNamePrefix = threadNamePrefix;
  }

  @Override
  public void preload() {
    log.debug("preload() - Start preloading data for cache " + cache.getName());
    Integer lastRow = batchCacheLoaderDAO.getMaxRow(); // 0 if no cache objects!

    cacheThreadPoolTaskExecutor.setThreadNamePrefix(this.threadNamePrefix);
    cacheThreadPoolTaskExecutor.initialize();

    Integer firstRow = 0;
    LinkedList<Callable<Object>> tasks = new LinkedList<Callable<Object>>();
    while (firstRow <= lastRow) {
      MapLoaderTask mapTask = new MapLoaderTask(firstRow + 1, firstRow + batchSize);
      tasks.push(mapTask);
      firstRow += batchSize;
    }
    try {
      cacheThreadPoolTaskExecutor.getThreadPoolExecutor().invokeAll(tasks, 1800, TimeUnit.SECONDS);
    } catch (RejectedExecutionException e) {
      log.error("Exception caught while loading a server cache from the database. This is probably due to the cache.loader.queue.size being"
          + "too small. Increase this to at least 'id range'/'cache loader batch size', or alternatively increase the"
          + "batch size.");
      throw e;
    } catch (InterruptedException e) {
      log.error("Interrupted while waiting for cache loading threads to terminate.", e);
    }
    cacheThreadPoolTaskExecutor.shutdown();
    log.debug("preload() - Finished preload for cache " + cache.getName());
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
        if (log.isTraceEnabled()) {
          log.trace("MapLoaderTask - Putting key {} to cache {}", key, cache.getName());
        }
        cache.putQuiet(new Element(key, cacheLoaderMap.get(key)));
      }
      return null;
    }
  }

}
