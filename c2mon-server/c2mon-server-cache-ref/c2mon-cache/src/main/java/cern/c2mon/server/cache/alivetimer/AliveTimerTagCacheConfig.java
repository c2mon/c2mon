package cern.c2mon.server.cache.alivetimer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;

/**
 * @author Szymon Halastra
 */

@Configuration
public class AliveTimerTagCacheConfig {

  @Bean(name = C2monCacheName.Names.ALIVETIMER)
  public C2monCache createCache(AbstractC2monCacheFactory cachingFactory) {
    return cachingFactory.createCache(C2monCacheName.ALIVETIMER.getLabel(), Long.class, CommFaultTag.class);
  }
}
