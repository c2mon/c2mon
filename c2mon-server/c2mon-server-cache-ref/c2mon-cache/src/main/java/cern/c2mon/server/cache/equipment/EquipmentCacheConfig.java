package cern.c2mon.server.cache.equipment;

import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */
@Configuration
public class EquipmentCacheConfig {

//  @Bean(name = CacheName.Names.EQUIPMENT)
//  public C2monCache createCache(AbstractCacheFactory cachingFactory, EquipmentDAO equipmentDAO) {
//    C2monCache cache = cachingFactory.createCache(CacheName.EQUIPMENT.getLabel(), Long.class, Equipment.class);
//
//    CacheLoader cacheLoader = new SimpleCacheLoader<>(cache, equipmentDAO);
//    cache.setCacheLoader(cacheLoader);
//
//    return cache;
//  }
}
