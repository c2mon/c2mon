package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.ehcache.CacheManager;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.CacheFactory;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class EquipmentCacheConfig {

  @Bean
  public CacheFactory equipmentEhcache(CacheManager cacheManager) {
    CacheFactory factory = new CacheFactory();
    factory.setCacheName("equipmentCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }

  @Bean
  public EhcacheLoaderImpl equipmentEhcacheLoader(Ehcache equipmentEhcache, EquipmentDAO equipmentDAO) {
    return new EhcacheLoaderImpl<>(equipmentEhcache, equipmentDAO);
  }

  @Bean
  public C2monCacheLoader equipmentCacheLoader(Ehcache equipmentEhcache, EquipmentDAO equipmentDAO) {
    return new SimpleC2monCacheLoader<>(equipmentEhcache, equipmentDAO);
  }
}
