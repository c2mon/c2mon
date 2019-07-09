package cern.c2mon.server.cache.alarm;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.server.cache.loader.BatchCacheLoaderDAO;
import cern.c2mon.server.cache.loader.common.BatchCacheLoader;
import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;
import cern.c2mon.shared.common.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


/**
 * @author Alexandros Papageorgiou Koufidis
 */
@Configuration
public class AbstractCacheConfig<V extends Cacheable> {

  private ThreadPoolTaskExecutor cacheLoaderTaskExecutor;
  private CacheLoaderProperties properties;
  private AbstractCacheFactory cachingFactory;

  @Autowired
  public AbstractCacheConfig(final ThreadPoolTaskExecutor cacheLoaderTaskExecutor, final CacheLoaderProperties properties, final AbstractCacheFactory cachingFactory) {
    this.cacheLoaderTaskExecutor = cacheLoaderTaskExecutor;
    this.properties = properties;
    this.cachingFactory = cachingFactory;
  }

  protected CacheLoader createCacheLoader(C2monCache<V> cache, BatchCacheLoaderDAO<V> batchCacheLoaderDAO, String threadNamePrefix) {
    return new BatchCacheLoader<>(cacheLoaderTaskExecutor, cache, batchCacheLoaderDAO,
      properties.getBatchSize(), threadNamePrefix);
  }

  protected C2monCache createCache(BatchCacheLoaderDAO<V> alarmLoaderDAORef, String cacheName, Class<V> classRef, String threadNamePrefix) {
    C2monCache<V> cache = cachingFactory.createCache(cacheName, classRef);

    cache.setCacheLoader(createCacheLoader(cache, alarmLoaderDAORef, threadNamePrefix));

    return cache;
  }
}
