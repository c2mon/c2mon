package cern.c2mon.server.cache.subequipment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractFactory;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.common.subequipment.SubEquipment;

/**
 * @author Szymon Halastra
 */

@Configuration
public class SubEquipmentCacheConfig {

  @Bean(name = C2monCacheName.Names.SUBEQUIPMENT)
  public C2monCache createCache(AbstractFactory cachingFactory) {
    return cachingFactory.createCache(C2monCacheName.SUBEQUIPMENT.getLabel(), Long.class, SubEquipment.class);
  }
}
