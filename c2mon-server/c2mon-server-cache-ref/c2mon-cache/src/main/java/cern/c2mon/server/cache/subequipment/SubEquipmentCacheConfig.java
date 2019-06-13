package cern.c2mon.server.cache.subequipment;

import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */

@Configuration
public class SubEquipmentCacheConfig {

//  @Bean(name = CacheName.Names.SUBEQUIPMENT)
//  public C2monCache createCache(AbstractCacheFactory cachingFactory, SubEquipmentDAO subEquipmentDAO) {
//    C2monCache cache = cachingFactory.createCache(CacheName.SUBEQUIPMENT.getLabel(), Long.class,
//            SubEquipment.class);
//
//    CacheLoader cacheLoader = new SimpleCacheLoader<>(cache, subEquipmentDAO);
//    cache.setCacheLoader(cacheLoader);
//
//    return cache;
//  }
}
