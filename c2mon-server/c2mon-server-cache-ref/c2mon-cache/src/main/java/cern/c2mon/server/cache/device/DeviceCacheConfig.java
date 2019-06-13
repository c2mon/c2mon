package cern.c2mon.server.cache.device;

import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */
@Configuration
public class DeviceCacheConfig {

//  @Bean(name = CacheName.Names.DEVICE)
//  public C2monCache createCache(AbstractCacheFactory cachingFactory, DeviceDAO deviceDAO) {
//    C2monCache cache = cachingFactory.createCache(CacheName.DEVICE.getLabel(), Long.class, Device.class);
//
//    CacheLoader cacheLoader = new SimpleCacheLoader<>(cache, deviceDAO);
//    cache.setCacheLoader(cacheLoader);
//
//    return cache;
//  }
}
