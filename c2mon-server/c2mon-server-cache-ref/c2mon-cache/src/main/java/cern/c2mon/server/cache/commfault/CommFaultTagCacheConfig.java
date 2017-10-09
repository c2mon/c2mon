package cern.c2mon.server.cache.commfault;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.cache.loader.CommFaultTagDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.server.common.commfault.CommFaultTag;

/**
 * @author Szymon Halastra
 */
@Configuration
public class CommFaultTagCacheConfig {

  @Bean(name = C2monCacheName.Names.COMMFAULT)
  public C2monCache createCache(AbstractC2monCacheFactory cachingFactory, CommFaultTagDAO commFaultTagDAO) {
    C2monCache cache = cachingFactory.createCache(C2monCacheName.COMMFAULT.getLabel(), Long.class, CommFaultTag.class);

    C2monCacheLoader cacheLoader = new SimpleCacheLoader(cache, commFaultTagDAO);
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
