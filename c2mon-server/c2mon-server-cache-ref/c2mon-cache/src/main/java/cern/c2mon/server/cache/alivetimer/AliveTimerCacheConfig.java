package cern.c2mon.server.cache.alivetimer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.cache.loader.AliveTimerDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;

/**
 * @author Szymon Halastra
 */

@Configuration
public class AliveTimerCacheConfig {

  @Autowired
  private ThreadPoolTaskExecutor cacheLoaderTaskExecutor;

  @Autowired
  private CacheLoaderProperties properties;

  @Bean(name = C2monCacheName.Names.ALIVETIMER)
  public C2monCache createCache(AbstractC2monCacheFactory cachingFactory, AliveTimerDAO aliveTimerDAORef) {
    C2monCache cache = cachingFactory.createCache(C2monCacheName.ALIVETIMER.getLabel(), Long.class, CommFaultTag.class);

    C2monCacheLoader cacheLoader = new SimpleCacheLoader<>(cache, aliveTimerDAORef);

    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
