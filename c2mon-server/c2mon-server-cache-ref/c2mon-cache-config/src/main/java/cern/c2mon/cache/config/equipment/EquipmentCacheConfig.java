package cern.c2mon.cache.config.equipment;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.config.CacheName;
import cern.c2mon.cache.config.config.AbstractSimpleCacheConfig;
import cern.c2mon.server.cache.loading.CacheLoaderDAO;
import cern.c2mon.server.common.equipment.Equipment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Configuration
public class EquipmentCacheConfig extends AbstractSimpleCacheConfig<Equipment> {

  @Inject
  public EquipmentCacheConfig(AbstractCacheFactory cachingFactory, CacheLoaderDAO<Equipment> cacheLoaderDAORef) {
    super(cachingFactory, CacheName.EQUIPMENT, Equipment.class, cacheLoaderDAORef);
  }

  @Bean(name = CacheName.Names.EQUIPMENT)
  @Override
  public C2monCache<Equipment> createCache() {
    return super.createCache();
  }
}
