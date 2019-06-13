package cern.c2mon.server.cache.process;

import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */
@Configuration
public class ProcessCacheConfigRef {

//  @Bean(name = CacheName.Names.PROCESS)
//  public C2monCache createCache(AbstractCacheFactory cachingFactory, ProcessDAO processDAO) {
//    C2monCache cache = cachingFactory.createCache(CacheName.PROCESS.getLabel(), Long.class, Process.class);
//
//    CacheLoader cacheLoader = new SimpleCacheLoader<>(cache, processDAO);
//    cache.setCacheLoader(cacheLoader);
//
//    return cache;
//  }
}
