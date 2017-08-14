package cern.c2mon.server.cache.alivetimer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractFactory;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;

/**
 * @author Szymon Halastra
 */

@Configuration
public class AliveTimerTagCacheConfig {

  public static final String ALIVE_TIMER_CACHE = "aliveTimerCacheRef";

  @Bean(name = ALIVE_TIMER_CACHE)
  public C2monCache createCache(AbstractFactory cachingFactory) {
    return cachingFactory.createCache(ALIVE_TIMER_CACHE, Long.class, CommFaultTag.class);
  }
}
