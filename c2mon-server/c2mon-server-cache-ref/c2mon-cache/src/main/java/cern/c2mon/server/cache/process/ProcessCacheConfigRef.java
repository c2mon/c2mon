package cern.c2mon.server.cache.process;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.cache.loader.ProcessDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.server.common.process.Process;

/**
 * @author Szymon Halastra
 */
@Configuration
public class ProcessCacheConfigRef {

  @Bean(name = C2monCacheName.Names.PROCESS)
  public Cache createCache(AbstractC2monCacheFactory cachingFactory, ProcessDAO processDAO) {
    Cache cache = cachingFactory.createCache(C2monCacheName.PROCESS.getLabel(), Long.class, Process.class);

    C2monCacheLoader cacheLoader = new SimpleCacheLoader<>(cache, processDAO);
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
