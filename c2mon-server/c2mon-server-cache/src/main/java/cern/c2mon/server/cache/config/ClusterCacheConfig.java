package cern.c2mon.server.cache.config;

import net.sf.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class ClusterCacheConfig {

  @Autowired
  private CacheManager cacheManager;

  @Bean
  public EhCacheFactoryBean clusterEhcache() {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
    factory.setCacheName("clusterCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }
}
