package cern.c2mon.server.cache.loader.common;


import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.server.cache.loader.BatchCacheLoaderDAO;
import cern.c2mon.shared.common.Cacheable;

/**
 * @author Szymon Halastra
 */
@Slf4j
public class BatchCacheLoader<K extends Number, V extends Cacheable> implements CacheLoader<K,V> {

  /**
   * Executor for loading the cache using multiple threads.
   */
  private final ThreadPoolTaskExecutor cacheLoaderTaskExecutor;

  /**
   * Timeout before an inactive thread is returned to the pool
   */
  private static final int THREAD_TIMEOUT = 5; //in seconds

  /**
   * The max number of cache object fetched in one query from the DB and loaded
   * into the cache in a single thread (covers this many ids, but all need not
   * be used).
   */
  private int batchSize;

  /**
   * Name prefix for ThreadPool threads
   */
  private String threadNamePrefix;

  /**
   * Reference to C2monCache
   */
  private final C2monCache<Long, V> c2monCache;

  /**
   * Reference to batch loader DAO
   */
  private final BatchCacheLoaderDAO<K, V> batchCacheLoaderDAO;

  /**
   * Constructor (used in Spring XML to instantiate the loaders
   * for the different caches).
   *
   * @param cacheLoaderTaskExecutor
   * @param c2monCache            the c2monCache to load from the DB
   * @param cacheLoaderDAO   the DAO for accessing the DB
   * @param batchSize        the number of object loaded in a single task
   * @param threadNamePrefix the name of thread pool
   */
  public BatchCacheLoader(ThreadPoolTaskExecutor cacheLoaderTaskExecutor, final C2monCache<Long, V> c2monCache,
                          final BatchCacheLoaderDAO<K, V> cacheLoaderDAO,
                          final int batchSize,
                          final String threadNamePrefix) {
    this.cacheLoaderTaskExecutor = cacheLoaderTaskExecutor;
    this.batchSize = batchSize;
    this.batchCacheLoaderDAO = cacheLoaderDAO;
    this.c2monCache = c2monCache;
    this.threadNamePrefix = threadNamePrefix;

    log.info("BatchCacheLoader after ref initialized");
  }

  @Override
  public void preload() {
    log.debug("preload() - Start preloading data for c2monCache " + c2monCache.getName());

    Integer lastRow = batchCacheLoaderDAO.getMaxRow(); // 0 if no cache objects!
    log.info("Preload is running for " + c2monCache.getName());

    cacheLoaderTaskExecutor.setThreadNamePrefix(this.threadNamePrefix);
    cacheLoaderTaskExecutor.initialize();

    Integer firstRow = 0;
    LinkedList<Callable<Object>> tasks = new LinkedList<>();
    while (firstRow <= lastRow) {
      MapLoaderTask mapTask = new MapLoaderTask(firstRow + 1, firstRow + batchSize);
      tasks.push(mapTask);
      firstRow += batchSize;
    }
    try {
      cacheLoaderTaskExecutor.getThreadPoolExecutor().invokeAll(tasks, 1800, TimeUnit.SECONDS);
    }
    catch (RejectedExecutionException e) {
      log.error("Exception caught while loading a server cache from the database. This is probably due to the cache.loader.queue.size being"
              + "too small. Increase this to at least 'id range'/'cache loader batch size', or alternatively increase the"
              + "batch size.");
      throw e;
    }
    catch (InterruptedException e) {
      log.error("Interrupted while waiting for cache loading threads to terminate.", e);
    }
    cacheLoaderTaskExecutor.shutdown();
    log.debug("preload() - Finished preload for cache " + c2monCache.getName());
  }

  /**
   * Task that loads a batch of cache objects into
   * the cache. A batch must be specified by a first
   * and last Cacheable id and the DAO must implement the
   * <code>getBatchAsMap</code> method.
   * <p>
   * <p>Returns null on successful completion.
   *
   * @author Mark Brightwell
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
      Map<Long, V> cacheLoaderMap = batchCacheLoaderDAO.getBatchAsMap(firstId, lastId);
      //preloadBuffer.putAll(cacheLoaderMap);
      for (Long key : cacheLoaderMap.keySet()) {
        if (log.isTraceEnabled()) {
          log.trace("MapLoaderTask - Putting key {} to cache {}", key, c2monCache.getName());
        }
        c2monCache.put(key, cacheLoaderMap.get(key));
      }
      return null;
    }
  }

}
