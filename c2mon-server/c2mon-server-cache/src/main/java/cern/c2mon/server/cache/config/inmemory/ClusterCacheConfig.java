package cern.c2mon.server.cache.config.inmemory;

import cern.c2mon.server.ehcache.CacheManager;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class ClusterCacheConfig {

  @Autowired
  private CacheManager cacheManager;

  @Bean
  public Ehcache clusterEhcache(){
    return new InMemoryCache("clusterCache");
  }
}
