package cern.c2mon.server.cache.command;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.cache.loader.CommandTagDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.shared.common.command.CommandTag;

/**
 * @author Szymon Halastra
 */
@Configuration
public class CommandTagCacheConfig {

  @Bean(name = C2monCacheName.Names.COMMAND)
  public Cache createCache(AbstractC2monCacheFactory cachingFactory, CommandTagDAO commandTagDAO) {
    Cache cache = cachingFactory.createCache(C2monCacheName.COMMAND.getLabel(), Long.class, CommandTag.class);

    C2monCacheLoader cacheLoader = new SimpleCacheLoader<>(cache, commandTagDAO);

    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
