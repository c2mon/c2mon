package cern.c2mon.cache.config.oscillation;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.config.CacheName;
import cern.c2mon.server.common.alarm.OscillationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OscillationCacheConfig {

  private final AbstractCacheFactory cachingFactory;

  @Autowired
  public OscillationCacheConfig(final AbstractCacheFactory cachingFactory) {
    this.cachingFactory = cachingFactory;
  }

  @Bean(name = CacheName.Names.ALARM_OSCILLATION)
  public C2monCache<OscillationTimestamp> createCache() {
    return cachingFactory.createCache(CacheName.Names.ALARM_OSCILLATION, OscillationTimestamp.class);
  }
}
