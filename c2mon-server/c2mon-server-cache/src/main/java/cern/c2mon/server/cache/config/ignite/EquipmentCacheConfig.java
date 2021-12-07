package cern.c2mon.server.cache.config.ignite;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class EquipmentCacheConfig {

  @Autowired
  private IgniteCacheProperties igniteCacheProperties;

  @Bean
  public Ehcache equipmentEhcache(){
    return new IgniteCacheImpl("equipmentCache", igniteCacheProperties);
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
