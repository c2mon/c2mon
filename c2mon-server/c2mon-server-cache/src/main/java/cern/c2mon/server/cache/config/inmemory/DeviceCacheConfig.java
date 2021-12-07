package cern.c2mon.server.cache.config.inmemory;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.device.query.DeviceInMemoryQuery;
import cern.c2mon.server.cache.device.query.DeviceQuery;
import cern.c2mon.server.cache.loading.DeviceDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class DeviceCacheConfig {

  @Bean
  public Ehcache deviceEhcache(){
    return new InMemoryCache("deviceCache");
  }

  @Bean
  public EhcacheLoaderImpl deviceEhcacheLoader(Ehcache deviceEhcache, DeviceDAO deviceDAO) {
    return new EhcacheLoaderImpl<>(deviceEhcache, deviceDAO);
  }

  @Bean
  public DeviceQuery deviceQuery(Ehcache deviceEhcache){
    return new DeviceInMemoryQuery(deviceEhcache);
  }


  @Bean
  public C2monCacheLoader deviceCacheLoader(Ehcache deviceEhcache, DeviceDAO deviceDAO) {
    return new SimpleC2monCacheLoader<>(deviceEhcache, deviceDAO);
  }
}
