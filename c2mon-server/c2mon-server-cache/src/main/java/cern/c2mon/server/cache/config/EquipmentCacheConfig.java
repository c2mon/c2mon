package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.cache.loading.ProcessDAO;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class EquipmentCacheConfig {

  @Bean
  public EhCacheFactoryBean equipmentEhcache(CacheManager cacheManager) {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
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
