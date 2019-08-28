package cern.c2mon.server.cache.subequipment;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.server.cache.AbstractSimpleCacheConfig;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.loader.CacheLoaderDAO;
import cern.c2mon.server.common.subequipment.SubEquipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */

@Configuration
public class SubEquipmentCacheConfig extends AbstractSimpleCacheConfig<SubEquipment> {

  @Autowired
  public SubEquipmentCacheConfig(AbstractCacheFactory cachingFactory, CacheLoaderDAO<SubEquipment> cacheLoaderDAORef) {
    super(cachingFactory, CacheName.SUBEQUIPMENT, SubEquipment.class, cacheLoaderDAORef);
  }

  @Bean(name = CacheName.Names.SUBEQUIPMENT)
  @Override
  public C2monCache<SubEquipment> createCache() {
    return super.createCache();
  }
}
