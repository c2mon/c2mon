package cern.c2mon.server.cache.config.inmemory;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.device.query.DeviceClassInMemoryQuery;
import cern.c2mon.server.cache.device.query.DeviceClassQuery;
import cern.c2mon.server.cache.loading.DeviceClassDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class DeviceClassCacheConfig {

  @Bean
  public Ehcache deviceClassEhcache(){
    return new InMemoryCache("deviceClassCache");
  }

  @Bean
  public EhcacheLoaderImpl deviceClassEhcacheLoader(Ehcache deviceClassEhcache, DeviceClassDAO deviceClassDAO) {
    return new EhcacheLoaderImpl<>(deviceClassEhcache, deviceClassDAO);
  }

  @Bean
  public DeviceClassQuery deviceClassQuery(Ehcache deviceClassEhcache){
    return new DeviceClassInMemoryQuery(deviceClassEhcache);
  }


  @Bean
  public C2monCacheLoader deviceClassCacheLoader(Ehcache deviceClassEhcache, DeviceClassDAO deviceClassDAO) {
    return new SimpleC2monCacheLoader<>(deviceClassEhcache, deviceClassDAO);
  }
}
