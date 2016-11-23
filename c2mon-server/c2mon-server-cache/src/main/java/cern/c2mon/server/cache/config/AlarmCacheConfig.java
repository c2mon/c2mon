package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.BatchCacheLoader;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.loading.AlarmLoaderDAO;
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
public class AlarmCacheConfig {

  @Autowired
  private CacheLoadingProperties properties;

  @Bean
  public EhCacheFactoryBean alarmEhcache(CacheManager cacheManager) {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
    factory.setCacheName("alarmCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }

  @Bean
  public EhcacheLoaderImpl alarmEhcacheLoader(Ehcache alarmEhcache, AlarmLoaderDAO alarmLoaderDAO) {
    return new EhcacheLoaderImpl<>(alarmEhcache, alarmLoaderDAO);
  }

  @Bean
  public C2monCacheLoader alarmCacheLoader(Ehcache alarmEhcache, AlarmLoaderDAO alarmLoaderDAO) {
    Integer batchSize = properties.getBatchSize();
    return new BatchCacheLoader<>(alarmEhcache, alarmLoaderDAO, batchSize, "AlarmCacheLoader-");
  }
}
