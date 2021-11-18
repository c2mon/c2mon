package cern.c2mon.server.cache.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import cern.c2mon.server.cache.loading.AlarmLoaderDAO;
import cern.c2mon.server.cache.loading.common.BatchCacheLoader;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.loading.config.CacheLoadingProperties;
import cern.c2mon.server.ehcache.CacheFactory;
import cern.c2mon.server.ehcache.CacheManager;
import cern.c2mon.server.ehcache.Ehcache;

/**
 * @author Justin Lewis Salmon
 */
public class AlarmCacheConfig {

  @Autowired
  private CacheLoadingProperties properties;

  @Bean
  public CacheFactory alarmEhcache(CacheManager cacheManager) {
    CacheFactory factory = new CacheFactory();
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
