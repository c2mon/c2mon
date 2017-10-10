package cern.c2mon.server.cache.equipment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.loader.EquipmentDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.server.common.equipment.Equipment;

/**
 * @author Szymon Halastra
 */
@Configuration
public class EquipmentCacheConfig {

  @Bean(name = CacheName.Names.EQUIPMENT)
  public Cache createCache(AbstractCacheFactory cachingFactory, EquipmentDAO equipmentDAO) {
    Cache cache = cachingFactory.createCache(CacheName.EQUIPMENT.getLabel(), Long.class, Equipment.class);

    CacheLoader cacheLoader = new SimpleCacheLoader<>(cache, equipmentDAO);
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
