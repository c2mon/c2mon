package cern.c2mon.server.cache.commfault;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.loader.CommFaultTagDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.server.common.commfault.CommFaultTag;

/**
 * @author Szymon Halastra
 */
@Configuration
public class CommFaultTagCacheConfig {

  @Bean(name = CacheName.Names.COMMFAULT)
  public Cache createCache(AbstractCacheFactory cachingFactory, CommFaultTagDAO commFaultTagDAO) {
    Cache cache = cachingFactory.createCache(CacheName.COMMFAULT.getLabel(), Long.class, CommFaultTag.class);

    C2monCacheLoader cacheLoader = new SimpleCacheLoader(cache, commFaultTagDAO);
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
