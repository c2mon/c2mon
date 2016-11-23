package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.BatchCacheLoader;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.loading.DataTagLoaderDAO;
import cern.c2mon.server.cache.loading.config.CacheLoadingProperties;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class DataTagCacheConfig {

  @Autowired
  private CacheLoadingProperties properties;

  @Bean
  public EhCacheFactoryBean dataTagEhcache(CacheManager cacheManager) {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
    factory.setCacheName("tagCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }

  @Bean
  public EhcacheLoaderImpl dataTagEhcacheLoader(Ehcache dataTagEhcache, DataTagLoaderDAO dataTagLoaderDAO) {
    return new EhcacheLoaderImpl<>(dataTagEhcache, dataTagLoaderDAO);
  }

  @Bean
  public C2monCacheLoader dataTagCacheLoader(Ehcache dataTagEhcache, DataTagLoaderDAO dataTagLoaderDAO) {
    Integer batchSize = properties.getBatchSize();
    return new BatchCacheLoader<>(dataTagEhcache, dataTagLoaderDAO, batchSize, "DataTagCacheLoader-");
  }
}
