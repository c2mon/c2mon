package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.DeviceDAO;
import cern.c2mon.server.ehcache.CacheManager;
import cern.c2mon.server.ehcache.Ehcache;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class DeviceCacheConfig {

  @Bean
  public EhCacheFactoryBean deviceEhcache(CacheManager cacheManager) {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
    factory.setCacheName("deviceCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }

  @Bean
  public EhcacheLoaderImpl deviceEhcacheLoader(Ehcache deviceEhcache, DeviceDAO deviceDAO) {
    return new EhcacheLoaderImpl<>(deviceEhcache, deviceDAO);
  }

  @Bean
  public C2monCacheLoader deviceCacheLoader(Ehcache deviceEhcache, DeviceDAO deviceDAO) {
    return new SimpleC2monCacheLoader<>(deviceEhcache, deviceDAO);
  }
}
