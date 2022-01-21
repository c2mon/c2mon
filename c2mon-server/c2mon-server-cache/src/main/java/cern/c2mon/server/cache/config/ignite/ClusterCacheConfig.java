package cern.c2mon.server.cache.config.ignite;

import cern.c2mon.server.ehcache.CacheManager;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class ClusterCacheConfig {

  @Autowired
  private CacheManager cacheManager;

  @Autowired
  private IgniteCacheProperties igniteCacheProperties;

  @Bean
  public Ehcache clusterEhcache(){
    CacheConfiguration cacheCfg = new CacheConfiguration();
    return new IgniteCacheImpl("clusterCache", igniteCacheProperties, cacheCfg);
  }
}
