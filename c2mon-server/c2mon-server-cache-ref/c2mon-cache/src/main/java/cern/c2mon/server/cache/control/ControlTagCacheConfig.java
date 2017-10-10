package cern.c2mon.server.cache.control;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.loader.ControlTagLoaderDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.server.common.control.ControlTag;

/**
 * @author Szymon Halastra
 */

@Configuration
public class ControlTagCacheConfig {

  @Bean(name = CacheName.Names.CONTROL)
  public Cache createCache(AbstractCacheFactory cachingFactory, ControlTagLoaderDAO controlTagLoaderDAO) {
    Cache cache = cachingFactory.createCache(CacheName.CONTROLTAG.getLabel(), Long.class, ControlTag.class);

    CacheLoader cacheLoader = new SimpleCacheLoader<>(cache, controlTagLoaderDAO);
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
