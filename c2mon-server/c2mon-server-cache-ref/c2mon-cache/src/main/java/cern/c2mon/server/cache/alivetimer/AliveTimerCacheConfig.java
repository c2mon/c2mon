package cern.c2mon.server.cache.alivetimer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.loader.AliveTimerDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;

/**
 * @author Szymon Halastra
 */

@Configuration
public class AliveTimerCacheConfig {

  @Bean(name = CacheName.Names.ALIVETIMER)
  public Cache createCache(AbstractCacheFactory cachingFactory, AliveTimerDAO aliveTimerDAORef) {
    Cache cache = cachingFactory.createCache(CacheName.ALIVETIMER.getLabel(), Long.class, CommFaultTag.class);

    C2monCacheLoader cacheLoader = new SimpleCacheLoader<>(cache, aliveTimerDAORef);

    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
