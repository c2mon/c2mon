package cern.c2mon.server.cache.config;

import org.springframework.context.annotation.Bean;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.AliveTimerDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.ehcache.CacheFactory;
import cern.c2mon.server.ehcache.CacheManager;
import cern.c2mon.server.ehcache.Ehcache;

/**
 * @author Justin Lewis Salmon
 */
public class AliveTimerCacheConfig {

  @Bean
  public CacheFactory aliveTimerEhcache(CacheManager cacheManager) {
    CacheFactory factory = new CacheFactory();
    factory.setCacheName("aliveTimerCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }

  @Bean
  public EhcacheLoaderImpl aliveTimerEhcacheLoader(Ehcache aliveTimerEhcache, AliveTimerDAO aliveTimerDAO) {
    return new EhcacheLoaderImpl<>(aliveTimerEhcache, aliveTimerDAO);
  }

  @Bean
  public C2monCacheLoader aliveTimerCacheLoader(Ehcache aliveTimerEhcache, AliveTimerDAO aliveTimerDAO) {
    return new SimpleC2monCacheLoader<>(aliveTimerEhcache, aliveTimerDAO);
  }
}
