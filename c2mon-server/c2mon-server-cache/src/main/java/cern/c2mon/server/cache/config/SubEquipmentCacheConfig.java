package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class SubEquipmentCacheConfig {

  @Bean
  public EhCacheFactoryBean subEquipmentEhcache(CacheManager cacheManager) {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
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
