package cern.c2mon.server.cache.config.ignite;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.device.query.DeviceIgniteQuery;
import cern.c2mon.server.cache.device.query.DeviceQuery;
import cern.c2mon.server.cache.loading.DeviceDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class DeviceCacheConfig {

  @Autowired
  private IgniteCacheProperties igniteCacheProperties;

  @Bean
  public Ehcache deviceEhcache(){
    CacheConfiguration cacheCfg = new CacheConfiguration();
    cacheCfg.setIndexedTypes(Long.class, DeviceCacheObject.class);
    return new IgniteCacheImpl("deviceCache", igniteCacheProperties, cacheCfg);
  }

  @Bean
  public EhcacheLoaderImpl deviceEhcacheLoader(Ehcache deviceEhcache, DeviceDAO deviceDAO) {
    return new EhcacheLoaderImpl<>(deviceEhcache, deviceDAO);
  }

  @Bean
  public DeviceQuery deviceQuery(Ehcache deviceEhcache){
    return new DeviceIgniteQuery(deviceEhcache);
  }


  @Bean
  public C2monCacheLoader deviceCacheLoader(Ehcache deviceEhcache, DeviceDAO deviceDAO) {
    return new SimpleC2monCacheLoader<>(deviceEhcache, deviceDAO);
  }
}
