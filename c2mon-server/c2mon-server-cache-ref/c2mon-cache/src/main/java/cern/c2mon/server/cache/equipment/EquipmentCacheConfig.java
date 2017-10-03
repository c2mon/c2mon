package cern.c2mon.server.cache.equipment;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.DbLoadable;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.cache.loader.EquipmentDAO;
import cern.c2mon.server.cache.loader.common.SimpleCacheLoader;
import cern.c2mon.server.common.equipment.Equipment;

/**
 * @author Szymon Halastra
 */
@Configuration
public class EquipmentCacheConfig {

  @Bean(name = C2monCacheName.Names.EQUIPMENT)
  public C2monCache createCache(AbstractC2monCacheFactory cachingFactory, EquipmentDAO equipmentDAO) {
    C2monCache cache = cachingFactory.createCache(C2monCacheName.EQUIPMENT.getLabel(), Long.class, Equipment.class);

    C2monCacheLoader cacheLoader = new SimpleCacheLoader<>(cache, equipmentDAO);
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
