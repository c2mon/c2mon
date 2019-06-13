package cern.c2mon.server.cache.deviceclass;

import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */

@Configuration
public class DeviceClassCacheConfig {

//  @Bean(name = CacheName.Names.DEVICECLASS)
//  public C2monCache createCache(AbstractCacheFactory cachingFactory, DeviceClassDAO deviceClassDAO) {
//    C2monCache cache = cachingFactory.createCache(CacheName.DEVICECLASS.getLabel(), Long.class,
//            DeviceClass.class);
//
//    CacheLoader cacheLoader = new SimpleCacheLoader<>(cache, deviceClassDAO);
//    cache.setCacheLoader(cacheLoader);
//
//    return cache;
//  }
}
