package cern.c2mon.server.cache.command;

import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */
@Configuration
public class CommandTagCacheConfig {

//  @Bean(name = CacheName.Names.COMMAND)
//  public C2monCache createCache(AbstractCacheFactory cachingFactory, CommandTagDAO commandTagDAO) {
//    C2monCache cache = cachingFactory.createCache(CacheName.COMMAND.getLabel(), Long.class, CommandTag.class);
//
//    CacheLoader cacheLoader = new SimpleCacheLoader<>(cache, commandTagDAO);
//
//    cache.setCacheLoader(cacheLoader);
//
//    return cache;
//  }
}
