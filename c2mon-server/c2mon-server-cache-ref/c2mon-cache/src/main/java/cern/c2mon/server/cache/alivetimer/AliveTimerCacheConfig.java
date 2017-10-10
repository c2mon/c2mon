package cern.c2mon.server.cache.alivetimer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.cache.loader.AliveTimerDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;

/**
 * @author Szymon Halastra
 */

@Configuration
public class AliveTimerCacheConfig {

  @Bean(name = C2monCacheName.Names.ALIVETIMER)
  public Cache createCache(AbstractC2monCacheFactory cachingFactory, AliveTimerDAO aliveTimerDAORef) {
    Cache cache = cachingFactory.createCache(C2monCacheName.ALIVETIMER.getLabel(), Long.class, CommFaultTag.class);

    C2monCacheLoader cacheLoader = new SimpleCacheLoader<>(cache, aliveTimerDAORef);

    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
