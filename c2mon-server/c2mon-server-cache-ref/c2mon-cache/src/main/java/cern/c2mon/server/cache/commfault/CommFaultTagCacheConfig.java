package cern.c2mon.server.cache.commfault;

import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */
@Configuration
public class CommFaultTagCacheConfig {

//  @Bean(name = CacheName.Names.COMMFAULT)
//  public C2monCache createCache(AbstractCacheFactory cachingFactory, CommFaultTagDAO commFaultTagDAO) {
//    C2monCache cache = cachingFactory.createCache(CacheName.COMMFAULT.getLabel(), Long.class, CommFaultTag.class);
//
//    CacheLoader cacheLoader = new SimpleCacheLoader(cache, commFaultTagDAO);
//    cache.setCacheLoader(cacheLoader);
//
//    return cache;
//  }
}
