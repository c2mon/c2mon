package cern.c2mon.server.jcacheref.prototype.device;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.jcacheref.prototype.common.BasicCache;

/**
 * @author Szymon Halastra
 */
@Configuration
public class DeviceCacheConfig implements BasicCache {

  private static final String DEVICE_CACHE = "deviceCacheRef";

  @Bean(name = DEVICE_CACHE)
  public Cache<Long, Device> getDeviceCache() {
//    CacheManager cm = cacheManager.getCacheManager();
//    return cm.getCache(DEVICE_CACHE, Long.class, Device.class);
    return null;
  }

  @Override
  public C2monCacheName getName() {
    return C2monCacheName.DEVICE;
  }
}
