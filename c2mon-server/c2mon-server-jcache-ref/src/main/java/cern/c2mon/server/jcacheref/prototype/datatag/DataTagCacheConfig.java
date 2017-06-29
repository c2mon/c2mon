package cern.c2mon.server.jcacheref.prototype.datatag;

import java.io.Serializable;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;

import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.jcacheref.prototype.common.BasicCache;

/**
 * @author Szymon Halastra
 */
public class DataTagCacheConfig implements BasicCache, Serializable {

  private static final String DATA_TAG_CACHE = "dataTagCache";

  @Bean(name = DATA_TAG_CACHE)
  public Cache<Long, DataTag> getDataTagCache(JCacheCacheManager cacheManager) {
    CacheManager cm = cacheManager.getCacheManager();

    return cm.getCache(DATA_TAG_CACHE, Long.class, DataTag.class);
  }

  @Override
  public C2monCacheName getName() {
    return C2monCacheName.DATATAG;
  }
}
