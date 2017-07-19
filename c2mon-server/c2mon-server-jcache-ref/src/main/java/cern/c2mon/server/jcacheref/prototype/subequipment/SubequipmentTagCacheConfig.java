package cern.c2mon.server.jcacheref.prototype.subequipment;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.jcacheref.prototype.common.BasicCache;

/**
 * @author Szymon Halastra
 */

@Configuration
public class SubequipmentTagCacheConfig implements BasicCache {

  private static final String SUBEQUIPMENT_TAG_CACHE = "subequipmentTagCache";

  @Bean(name = SUBEQUIPMENT_TAG_CACHE)
  public Cache<Long, SubEquipment> getSubequipmentTagCache() {
//    CacheManager cm = cacheManager.getCacheManager();
//    return cm.getCache(SUBEQUIPMENT_TAG_CACHE, Long.class, SubEquipment.class);

    return null;
  }

  @Override
  public C2monCacheName getName() {
    return C2monCacheName.SUBEQUIPMENT;
  }
}
