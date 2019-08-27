package cern.c2mon.server.cache.subequipment;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.alarm.AbstractCacheConfig;
import cern.c2mon.server.cache.loader.SubEquipmentDAO;
import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;
import cern.c2mon.server.common.subequipment.SubEquipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Szymon Halastra
 */

@Configuration
public class SubEquipmentCacheConfig extends AbstractCacheConfig<SubEquipment> {

  public SubEquipmentCacheConfig(ThreadPoolTaskExecutor cacheLoaderTaskExecutor, CacheLoaderProperties properties, AbstractCacheFactory cachingFactory) {
    super(cacheLoaderTaskExecutor, properties, cachingFactory);
  }

//  TODO Fix this signature to properly handle BatchCacheLoaderDAO (see AlarmCacheConfig) and SimpleCacheLoaderDAO
//  @Bean(name = CacheName.Names.SUBEQUIPMENT)
//  @Autowired
//  public C2monCache<SubEquipment> createCache(SubEquipmentDAO subEquipmentDAO) {
//    return super.createCache(subEquipmentDAO, CacheName.SUBEQUIPMENT.getLabel(), SubEquipment.class, "SubEquipmentCacheLoader-");
//  }
}
