package cern.c2mon.server.cache.alarm.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.alarm.AlarmServiceTimestamp;
import cern.c2mon.server.cache.config.AbstractBatchCacheConfig;
import cern.c2mon.server.cache.config.AbstractCacheConfig;
import cern.c2mon.server.cache.loader.BatchCacheLoaderDAO;
import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class TimestampCacheConfig extends AbstractBatchCacheConfig<AlarmServiceTimestamp> {

  @Autowired
  public TimestampCacheConfig(AbstractCacheFactory cachingFactory, ThreadPoolTaskExecutor cacheLoaderTaskExecutor, CacheLoaderProperties properties, BatchCacheLoaderDAO<AlarmServiceTimestamp> batchCacheLoaderDAORef) {
    super(cachingFactory, CacheName.ALARM_TIMESTAMP, AlarmServiceTimestamp.class, cacheLoaderTaskExecutor, properties, batchCacheLoaderDAORef);
  }

  @Override
  @Bean(name = CacheName.Names.ALARM_TIMESTAMP)
  public C2monCache<AlarmServiceTimestamp> createCache() {
    return super.createCache();
  }
}
