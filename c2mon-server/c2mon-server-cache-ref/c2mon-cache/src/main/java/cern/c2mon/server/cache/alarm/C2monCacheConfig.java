package cern.c2mon.server.cache.alarm;

import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.server.cache.C2monCacheFactory;
import cern.c2mon.cache.api.C2monCache;
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
public abstract class C2monCacheConfig<V extends Cacheable> {

  @Autowired
  protected ThreadPoolTaskExecutor cacheLoaderTaskExecutor;

  @Autowired
  private CacheLoaderProperties properties;

  @Autowired
  protected C2monCacheFactory cachingFactory;

  protected C2monCache<V> createCache(BatchCacheLoaderDAO<Long, V> alarmLoaderDAORef, String cacheName, Class<V> classRef, String threadNamePrefix) {
    C2monCache<V> cache = cachingFactory.createCache(cacheName, classRef);

    cache.setCacheLoader(createCacheLoader(cache, alarmLoaderDAORef, threadNamePrefix));

    return cache;
  }

  protected CacheLoader<Long,V> createCacheLoader(C2monCache<V> cache, BatchCacheLoaderDAO<Long, V> batchCacheLoaderDAO, String threadNamePrefix) {
    return new BatchCacheLoader<>(cacheLoaderTaskExecutor, cache, batchCacheLoaderDAO,
      properties.getBatchSize(), threadNamePrefix);
  }
}
