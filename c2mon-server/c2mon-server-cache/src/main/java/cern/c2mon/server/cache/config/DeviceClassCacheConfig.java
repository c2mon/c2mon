package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.DeviceClassDAO;
import cern.c2mon.server.ehcache.CacheManager;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.CacheFactory;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class DeviceClassCacheConfig {

  @Bean
  public CacheFactory deviceClassEhcache(CacheManager cacheManager) {
    CacheFactory factory = new CacheFactory();
    factory.setCacheName("deviceClassCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }

  @Bean
  public EhcacheLoaderImpl deviceClassEhcacheLoader(Ehcache deviceClassEhcache, DeviceClassDAO deviceClassDAO) {
    return new EhcacheLoaderImpl<>(deviceClassEhcache, deviceClassDAO);
  }

  @Bean
  public C2monCacheLoader deviceClassCacheLoader(Ehcache deviceClassEhcache, DeviceClassDAO deviceClassDAO) {
    return new SimpleC2monCacheLoader<>(deviceClassEhcache, deviceClassDAO);
  }
}
