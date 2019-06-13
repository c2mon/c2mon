package cern.c2mon.server.cache.control;

import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */

@Configuration
public class ControlTagCacheConfig {

//  @Bean(name = CacheName.Names.CONTROL)
//  public C2monCache createCache(AbstractCacheFactory cachingFactory, ControlTagLoaderDAO controlTagLoaderDAO) {
//    C2monCache cache = cachingFactory.createCache(CacheName.CONTROLTAG.getLabel(), Long.class, ControlTag.class);
//
//    CacheLoader cacheLoader = new SimpleCacheLoader<>(cache, controlTagLoaderDAO);
//    cache.setCacheLoader(cacheLoader);
//
//    return cache;
//  }
}
