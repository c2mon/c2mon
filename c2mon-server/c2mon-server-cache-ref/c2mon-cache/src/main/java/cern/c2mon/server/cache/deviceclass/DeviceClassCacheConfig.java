package cern.c2mon.server.cache.deviceclass;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.loader.DeviceClassDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.server.common.device.DeviceClass;

/**
 * @author Szymon Halastra
 */

@Configuration
public class DeviceClassCacheConfig {

  @Bean(name = CacheName.Names.DEVICECLASS)
  public Cache createCache(AbstractCacheFactory cachingFactory, DeviceClassDAO deviceClassDAO) {
    Cache cache = cachingFactory.createCache(CacheName.DEVICECLASS.getLabel(), Long.class,
            DeviceClass.class);

    C2monCacheLoader cacheLoader = new SimpleCacheLoader<>(cache, deviceClassDAO);
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
