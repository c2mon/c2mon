package cern.c2mon.server.cache.control;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.cache.loader.ControlTagLoaderDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.server.common.control.ControlTag;

/**
 * @author Szymon Halastra
 */

@Configuration
public class ControlTagCacheConfig {

  @Bean(name = C2monCacheName.Names.CONTROL)
  public C2monCache createCache(AbstractC2monCacheFactory cachingFactory, ControlTagLoaderDAO controlTagLoaderDAO) {
    C2monCache cache = cachingFactory.createCache(C2monCacheName.CONTROLTAG.getLabel(), Long.class, ControlTag.class);

    C2monCacheLoader cacheLoader = new SimpleCacheLoader<>(cache, controlTagLoaderDAO);
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
