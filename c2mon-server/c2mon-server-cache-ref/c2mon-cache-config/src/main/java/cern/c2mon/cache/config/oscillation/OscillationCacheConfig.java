package cern.c2mon.cache.config.oscillation;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.config.CacheName;
import cern.c2mon.server.common.alarm.OscillationTimestamp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

@Configuration
public class OscillationCacheConfig {

  private final AbstractCacheFactory cachingFactory;

  @Inject
  public OscillationCacheConfig(final AbstractCacheFactory cachingFactory) {
    this.cachingFactory = cachingFactory;
  }

  @Bean(name = CacheName.Names.ALARM_OSCILLATION)
  public C2monCache<OscillationTimestamp> createCache() {
    return cachingFactory.createCache(CacheName.Names.ALARM_OSCILLATION, OscillationTimestamp.class);
  }
}
