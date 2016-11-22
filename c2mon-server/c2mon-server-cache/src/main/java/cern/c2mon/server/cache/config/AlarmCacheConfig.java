package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.common.BatchCacheLoader;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.loading.AlarmLoaderDAO;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class AlarmCacheConfig {

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
  public C2monCacheLoader alarmCacheLoader(Ehcache alarmEhcache, AlarmLoaderDAO alarmLoaderDAO, Environment environment) {
    Integer batchSize = environment.getRequiredProperty("c2mon.server.cacheloading.batchSize", Integer.class);
    return new BatchCacheLoader<>(alarmEhcache, alarmLoaderDAO, batchSize, "AlarmCacheLoader-");
  }
}
