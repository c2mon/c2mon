package cern.c2mon.server.cache.config.ignite;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
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
public class SubEquipmentCacheConfig {

  @Autowired
  private IgniteCacheProperties igniteCacheProperties;

  @Bean
  public Ehcache subEquipmentEhcache(){
    return new IgniteCacheImpl("subEquipmentCache", igniteCacheProperties);
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
