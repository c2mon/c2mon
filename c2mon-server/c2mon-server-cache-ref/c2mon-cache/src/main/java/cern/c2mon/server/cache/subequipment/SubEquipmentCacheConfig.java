package cern.c2mon.server.cache.subequipment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.loader.SubEquipmentDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.server.common.subequipment.SubEquipment;

/**
 * @author Szymon Halastra
 */

@Configuration
public class SubEquipmentCacheConfig {

  @Bean(name = CacheName.Names.SUBEQUIPMENT)
  public Cache createCache(AbstractCacheFactory cachingFactory, SubEquipmentDAO subEquipmentDAO) {
    Cache cache = cachingFactory.createCache(CacheName.SUBEQUIPMENT.getLabel(), Long.class,
            SubEquipment.class);

    C2monCacheLoader cacheLoader = new SimpleCacheLoader<>(cache, subEquipmentDAO);
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
