package cern.c2mon.server.cache.config.ignite;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.AliveTimerDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class AliveTimerCacheConfig {

  @Autowired
  private IgniteCacheProperties igniteCacheProperties;

  @Bean
  public Ehcache aliveTimerEhcache(){
    return new IgniteCacheImpl("aliveTimerCache", igniteCacheProperties);
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
