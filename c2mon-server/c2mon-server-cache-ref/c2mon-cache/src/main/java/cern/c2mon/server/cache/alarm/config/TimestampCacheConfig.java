package cern.c2mon.server.cache.alarm.config;

import cern.c2mon.cache.api.C2monCacheBase;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.server.cache.Timestamp;
import cern.c2mon.server.cache.alarm.AbstractCacheConfig;
import cern.c2mon.server.cache.loader.BatchCacheLoaderDAO;
import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class TimestampCacheConfig extends AbstractCacheConfig<Timestamp> {

  public TimestampCacheConfig(ThreadPoolTaskExecutor cacheLoaderTaskExecutor, CacheLoaderProperties properties, AbstractCacheFactory cachingFactory) {
    super(cacheLoaderTaskExecutor, properties, cachingFactory);
  }

  @Bean(name = "timestampCacheRef")
  @Autowired
  public C2monCacheBase<Timestamp> createCache(BatchCacheLoaderDAO<Timestamp> timestampLoaderDao) {
    // TODO This should declare the cache with a CacheName
    return super.createCache(timestampLoaderDao, "timestampCacheRef", Timestamp.class, "TimestampCacheLoader-");
  }
}
