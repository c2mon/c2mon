package cern.c2mon.server.jcacheref.prototype.equipment;

import java.io.Serializable;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;

import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.jcacheref.prototype.common.BasicCache;

/**
 * @author Szymon Halastra
 */

@Configuration
public class EquipmentCacheConfig implements BasicCache, Serializable {

  private static final String EQUIPMENT_TAG_CACHE = "equipmentTagCache";

  @Bean(name = EQUIPMENT_TAG_CACHE)
  public Cache<Long, Equipment> getEquipmentTagCache(JCacheCacheManager cacheManager) {
    CacheManager cm = cacheManager.getCacheManager();
    MutableConfiguration<Long, Equipment> config = new MutableConfiguration<>();
    config.setTypes(Long.class, Equipment.class);

    return cm.createCache(EQUIPMENT_TAG_CACHE, config);
  }

  @Override
  public C2monCacheName getName() {
    return C2monCacheName.EQUIPMENT;
  }
}
