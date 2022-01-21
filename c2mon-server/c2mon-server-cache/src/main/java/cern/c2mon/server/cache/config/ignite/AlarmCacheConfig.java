package cern.c2mon.server.cache.config.ignite;

import cern.c2mon.server.cache.alarm.query.AlarmIgniteQuery;
import cern.c2mon.server.cache.alarm.query.AlarmQuery;
import cern.c2mon.server.cache.loading.AlarmLoaderDAO;
import cern.c2mon.server.cache.loading.common.BatchCacheLoader;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.loading.config.CacheLoadingProperties;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class AlarmCacheConfig {

  @Autowired
  private CacheLoadingProperties properties;

  @Autowired
  private IgniteCacheProperties igniteCacheProperties;

  @Bean
  public Ehcache alarmEhcache(){
    CacheConfiguration cacheCfg = new CacheConfiguration();
    cacheCfg.setIndexedTypes(Long.class, AlarmCacheObject.class);
    return new IgniteCacheImpl("alarmCache", igniteCacheProperties, cacheCfg);
  }

  @Bean
  public AlarmQuery alarmQuery(Ehcache alarmEhcache){
    return new AlarmIgniteQuery(alarmEhcache);
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
