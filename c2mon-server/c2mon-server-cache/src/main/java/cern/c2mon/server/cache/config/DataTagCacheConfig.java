package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.common.BatchCacheLoader;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.loading.AlarmLoaderDAO;
import cern.c2mon.server.cache.loading.DataTagLoaderDAO;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class DataTagCacheConfig {

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
  public C2monCacheLoader dataTagCacheLoader(Ehcache dataTagEhcache, DataTagLoaderDAO dataTagLoaderDAO, Environment environment) {
    Integer batchSize = environment.getRequiredProperty("c2mon.server.cacheloading.batchSize", Integer.class);
    return new BatchCacheLoader<>(dataTagEhcache, dataTagLoaderDAO, batchSize, "DataTagCacheLoader-");
  }
}
