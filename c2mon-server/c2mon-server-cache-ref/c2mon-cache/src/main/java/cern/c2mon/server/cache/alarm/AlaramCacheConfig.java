package cern.c2mon.server.cache.alarm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractFactory;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.common.alarm.Alarm;

/**
 * @author Szymon Halastra
 */
@Configuration
public class AlaramCacheConfig {

  @Bean(name = C2monCacheName.Names.ALARM)
  public C2monCache createCache(AbstractFactory cachingFactory) {
    return cachingFactory.createCache(C2monCacheName.ALARM.getLabel(), Long.class, Alarm.class);
  }
}
