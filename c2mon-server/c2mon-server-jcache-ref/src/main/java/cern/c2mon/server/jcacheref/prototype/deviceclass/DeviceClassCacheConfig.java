package cern.c2mon.server.jcacheref.prototype.deviceclass;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.jcacheref.prototype.common.BasicCache;

/**
 * @author Szymon Halastra
 */

@Configuration
public class DeviceClassCacheConfig implements BasicCache {

  private static final String DEVICE_CLASS_CACHE = "deviceClassCache";

  @Bean(name = DEVICE_CLASS_CACHE)
  public Cache<Long, DeviceClass> getDeviceClassCache(JCacheCacheManager cacheManager) {
    CacheManager cm = cacheManager.getCacheManager();
    return cm.getCache(DEVICE_CLASS_CACHE, Long.class, DeviceClass.class);
  }

  @Override
  public C2monCacheName getName() {
    return C2monCacheName.DEVICECLASS;
  }
}
