package cern.c2mon.server.cache.config.ignite;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.device.query.DeviceClassIgniteQuery;
import cern.c2mon.server.cache.device.query.DeviceClassQuery;
import cern.c2mon.server.cache.loading.DeviceClassDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class DeviceClassCacheConfig {

  @Autowired
  private IgniteCacheProperties igniteCacheProperties;

  @Bean
  public Ehcache deviceClassEhcache(){
    return new IgniteCacheImpl("deviceClassCache", igniteCacheProperties);
  }

  @Bean
  public EhcacheLoaderImpl deviceClassEhcacheLoader(Ehcache deviceClassEhcache, DeviceClassDAO deviceClassDAO) {
    return new EhcacheLoaderImpl<>(deviceClassEhcache, deviceClassDAO);
  }

  @Bean
  public DeviceClassQuery deviceClassQuery(Ehcache deviceClassEhcache){
    return new DeviceClassIgniteQuery(deviceClassEhcache);
  }


  @Bean
  public C2monCacheLoader deviceClassCacheLoader(Ehcache deviceClassEhcache, DeviceClassDAO deviceClassDAO) {
    return new SimpleC2monCacheLoader<>(deviceClassEhcache, deviceClassDAO);
  }
}
