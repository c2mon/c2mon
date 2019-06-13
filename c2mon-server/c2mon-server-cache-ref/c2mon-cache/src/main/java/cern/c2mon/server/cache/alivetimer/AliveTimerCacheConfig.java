package cern.c2mon.server.cache.alivetimer;

import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */

@Configuration
public class AliveTimerCacheConfig {

//  @Bean(name = CacheName.Names.ALIVETIMER)
//  public C2monCache createCache(AbstractCacheFactory cachingFactory, AliveTimerDAO aliveTimerDAORef) {
//    C2monCache cache = cachingFactory.createCache(CacheName.ALIVETIMER.getLabel(), Long.class, CommFaultTag.class);
//
//    CacheLoader cacheLoader = new SimpleCacheLoader<>(cache, aliveTimerDAORef);
//
//    cache.setCacheLoader(cacheLoader);
//
//    return cache;
//  }
}
