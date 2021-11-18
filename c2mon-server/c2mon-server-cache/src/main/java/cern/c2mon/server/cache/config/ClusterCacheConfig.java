package cern.c2mon.server.cache.config;

import cern.c2mon.server.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import cern.c2mon.server.ehcache.CacheFactory;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class ClusterCacheConfig {

  @Autowired
  private CacheManager cacheManager;

  @Bean
  public CacheFactory clusterEhcache() {
    CacheFactory factory = new CacheFactory();
    factory.setCacheName("clusterCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }
}
