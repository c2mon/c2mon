package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.ehcache.CacheManager;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.CacheFactory;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class SubEquipmentCacheConfig {

  @Bean
  public CacheFactory subEquipmentEhcache(CacheManager cacheManager) {
    CacheFactory factory = new CacheFactory();
    factory.setCacheName("subEquipmentCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }

  @Bean
  public EhcacheLoaderImpl subEquipmentEhcacheLoader(Ehcache subEquipmentEhcache, SubEquipmentDAO subEquipmentDAO) {
    return new EhcacheLoaderImpl<>(subEquipmentEhcache, subEquipmentDAO);
  }

  @Bean
  public C2monCacheLoader subEquipmentCacheLoader(Ehcache subEquipmentEhcache, SubEquipmentDAO subEquipmentDAO) {
    return new SimpleC2monCacheLoader<>(subEquipmentEhcache, subEquipmentDAO);
  }
}
