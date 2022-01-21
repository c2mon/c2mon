package cern.c2mon.server.cache.config.ignite;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import org.apache.ignite.configuration.CacheConfiguration;
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
    CacheConfiguration cacheCfg = new CacheConfiguration();
    cacheCfg.setIndexedTypes(Long.class, SubEquipmentCacheObject.class);
    return new IgniteCacheImpl("subEquipmentCache", igniteCacheProperties, cacheCfg);
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
