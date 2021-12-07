package cern.c2mon.server.cache.config.inmemory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import cern.c2mon.server.cache.alarm.query.AlarmIgniteQuery;
import cern.c2mon.server.cache.alarm.query.AlarmInMemoryQuery;
import cern.c2mon.server.cache.alarm.query.AlarmQuery;
import cern.c2mon.server.cache.loading.AlarmLoaderDAO;
import cern.c2mon.server.cache.loading.common.BatchCacheLoader;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.loading.config.CacheLoadingProperties;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

/**
 * @author Justin Lewis Salmon
 */
public class AlarmCacheConfig {

  @Autowired
  private CacheLoadingProperties properties;

  @Bean
  public Ehcache alarmEhcache(){
    return new InMemoryCache("alarmCache");
  }

  @Bean
  public AlarmQuery alarmQuery(Ehcache alarmEhcache){
    return new AlarmInMemoryQuery(alarmEhcache);
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
