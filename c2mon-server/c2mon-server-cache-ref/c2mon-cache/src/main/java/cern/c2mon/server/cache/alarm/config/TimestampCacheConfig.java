package cern.c2mon.server.cache.alarm.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.common.alarm.AlarmServiceTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// TODO Populating this?
// Generally this cache needs a review of biz functionality
public class TimestampCacheConfig {

  private final AbstractCacheFactory cachingFactory;

  @Autowired
  public TimestampCacheConfig(final AbstractCacheFactory cachingFactory) {
    this.cachingFactory = cachingFactory;
  }

  @Bean(name = CacheName.Names.ALARM_TIMESTAMP)
  public C2monCache<AlarmServiceTimestamp> createCache() {
    return cachingFactory.createCache(CacheName.Names.ALARM_TIMESTAMP, AlarmServiceTimestamp.class);
  }
}
