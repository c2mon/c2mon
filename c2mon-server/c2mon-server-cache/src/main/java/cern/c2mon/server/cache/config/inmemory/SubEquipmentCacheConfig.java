package cern.c2mon.server.cache.config.inmemory;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class SubEquipmentCacheConfig {

  @Bean
  public Ehcache subEquipmentEhcache(){
    return new InMemoryCache("subEquipmentCache");
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
