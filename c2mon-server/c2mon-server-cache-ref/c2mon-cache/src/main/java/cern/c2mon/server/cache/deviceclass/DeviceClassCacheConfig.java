package cern.c2mon.server.cache.deviceclass;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.server.cache.AbstractSimpleCacheConfig;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.loader.CacheLoaderDAO;
import cern.c2mon.server.common.device.DeviceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */

@Configuration
public class DeviceClassCacheConfig extends AbstractSimpleCacheConfig<DeviceClass> {

  @Autowired
  public DeviceClassCacheConfig(AbstractCacheFactory cachingFactory, CacheLoaderDAO<DeviceClass> cacheLoaderDAORef) {
    super(cachingFactory, CacheName.DEVICECLASS, DeviceClass.class, cacheLoaderDAORef);
  }

  @Bean(name = CacheName.Names.DEVICECLASS)
  @Override
  public C2monCache<DeviceClass> createCache() {
    return super.createCache();
  }
}
