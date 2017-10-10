package cern.c2mon.server.cache.device;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.loader.DeviceDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.server.common.device.Device;

/**
 * @author Szymon Halastra
 */
@Configuration
public class DeviceCacheConfig {

  @Bean(name = CacheName.Names.DEVICE)
  public Cache createCache(AbstractCacheFactory cachingFactory, DeviceDAO deviceDAO) {
    Cache cache = cachingFactory.createCache(CacheName.DEVICE.getLabel(), Long.class, Device.class);

    CacheLoader cacheLoader = new SimpleCacheLoader<>(cache, deviceDAO);
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
