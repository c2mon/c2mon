package cern.c2mon.server.cache.device;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.cache.loader.DeviceDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.server.common.device.Device;

/**
 * @author Szymon Halastra
 */
@Configuration
public class DeviceCacheConfig {

  @Bean(name = C2monCacheName.Names.DEVICE)
  public C2monCache createCache(AbstractC2monCacheFactory cachingFactory, DeviceDAO deviceDAO) {
    C2monCache cache = cachingFactory.createCache(C2monCacheName.DEVICE.getLabel(), Long.class, Device.class);

    C2monCacheLoader cacheLoader = new SimpleCacheLoader<>(cache, deviceDAO);
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
