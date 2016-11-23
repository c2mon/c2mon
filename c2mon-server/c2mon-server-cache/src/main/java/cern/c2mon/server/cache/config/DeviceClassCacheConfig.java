package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.DeviceClassDAO;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class DeviceClassCacheConfig {

  @Bean
  public EhCacheFactoryBean deviceClassEhcache(CacheManager cacheManager) {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
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
