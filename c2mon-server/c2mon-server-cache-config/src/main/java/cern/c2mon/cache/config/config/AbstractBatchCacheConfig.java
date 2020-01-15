package cern.c2mon.cache.config.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.config.CacheName;
import cern.c2mon.server.cache.loading.BatchCacheLoaderDAO;
import cern.c2mon.server.cache.loading.common.BatchCacheLoader;
import cern.c2mon.server.cache.loading.config.CacheLoadingProperties;
import cern.c2mon.shared.common.Cacheable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 *
 * @param <V>
 * @author Alexandros Papageorgiou Koufidis
 */
public class AbstractBatchCacheConfig<V extends Cacheable> extends AbstractCacheConfig<V, BatchCacheLoader<V>> {
  private static final String LOADER = "-Loader-";
  private final String THREAD_NAME_PREFIX;
  private final BatchCacheLoaderDAO<V> batchCacheLoaderDAORef;
  private final ThreadPoolTaskExecutor cacheLoaderTaskExecutor;
  private final CacheLoadingProperties properties;

  public AbstractBatchCacheConfig(AbstractCacheFactory cachingFactory, final CacheName cacheName, final Class<V> classRef,
                                  ThreadPoolTaskExecutor cacheLoaderTaskExecutor, CacheLoadingProperties properties,
                                  BatchCacheLoaderDAO<V> batchCacheLoaderDAORef) {
    super(cachingFactory, cacheName, classRef);
    this.cacheLoaderTaskExecutor = cacheLoaderTaskExecutor;
    this.properties = properties;
    this.batchCacheLoaderDAORef = batchCacheLoaderDAORef;
    THREAD_NAME_PREFIX = cacheName.getLabel() + LOADER;
  }

  @Override
  protected BatchCacheLoader<V> createCacheLoader(C2monCache<V> cache) {
    return new BatchCacheLoader<>(cacheLoaderTaskExecutor, cache, batchCacheLoaderDAORef, properties.getBatchSize(), THREAD_NAME_PREFIX);
  }

}