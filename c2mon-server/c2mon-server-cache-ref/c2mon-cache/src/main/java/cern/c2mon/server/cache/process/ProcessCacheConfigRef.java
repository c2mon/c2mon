package cern.c2mon.server.cache.process;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.loader.ProcessDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.server.common.process.Process;

/**
 * @author Szymon Halastra
 */
@Configuration
public class ProcessCacheConfigRef {

  @Bean(name = CacheName.Names.PROCESS)
  public Cache createCache(AbstractCacheFactory cachingFactory, ProcessDAO processDAO) {
    Cache cache = cachingFactory.createCache(CacheName.PROCESS.getLabel(), Long.class, Process.class);

    C2monCacheLoader cacheLoader = new SimpleCacheLoader<>(cache, processDAO);
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
