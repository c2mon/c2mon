package cern.c2mon.server.cache.loader.common;


import java.util.LinkedList;
import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.loader.BatchCacheLoaderDAO;
import cern.c2mon.shared.common.Cacheable;

/**
 * @author Szymon Halastra
 */
@Slf4j
public class BatchCacheLoader<K, V extends Cacheable> implements C2monCacheLoader {

  /**
   * Executor for loading the cache using multiple threads.
   */
  @Autowired
  private ThreadPoolTaskExecutor cacheLoadingThreadPoolTaskExecutor;

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
  private final C2monCache<K, V> cache;

  /**
   * Reference to batch loader DAO
   */
  private final BatchCacheLoaderDAO<V> batchCacheLoaderDAO;

  /**
   * Constructor (used in Spring XML to instantiate the loaders
   * for the different caches).
   *
   * @param cache          the cache to load from the DB
   * @param cacheLoaderDAO the DAO for accessing the DB
   *                       //   * @param batchSize        the number of object loaded in a single task
   *                       //   * @param threadNamePrefix the name of thread pool
   */
  public BatchCacheLoader(final C2monCache<K, V> cache,
                          final BatchCacheLoaderDAO<V> cacheLoaderDAO) {
    this.batchCacheLoaderDAO = cacheLoaderDAO;
    this.cache = cache;
  }

  @Override
  public void preload() {
    log.debug("preload() - Start preloading data for cache " + cache.getName());
    Integer lastRow = batchCacheLoaderDAO.getMaxRow(); // 0 if no cache objects!

    cacheLoadingThreadPoolTaskExecutor.setThreadNamePrefix(this.threadNamePrefix);
    cacheLoadingThreadPoolTaskExecutor.initialize();

    Integer firstRow = 0;
    LinkedList<Callable<Object>> tasks = new LinkedList<>();
  }
}
