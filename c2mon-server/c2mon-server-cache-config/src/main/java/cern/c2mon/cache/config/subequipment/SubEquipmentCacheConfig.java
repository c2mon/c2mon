package cern.c2mon.cache.config.subequipment;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.config.CacheName;
import cern.c2mon.cache.config.config.AbstractSimpleCacheConfig;
import cern.c2mon.server.cache.loading.CacheLoaderDAO;
import cern.c2mon.server.common.subequipment.SubEquipment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */

@Configuration
public class SubEquipmentCacheConfig extends AbstractSimpleCacheConfig<SubEquipment> {

  @Inject
  public SubEquipmentCacheConfig(AbstractCacheFactory cachingFactory, CacheLoaderDAO<SubEquipment> cacheLoaderDAORef) {
    super(cachingFactory, CacheName.SUBEQUIPMENT, SubEquipment.class, cacheLoaderDAORef);
  }

  @Bean(name = CacheName.Names.SUBEQUIPMENT)
  @Override
  public C2monCache<SubEquipment> createCache() {
    return super.createCache();
  }
}
