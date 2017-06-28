package cern.c2mon.server.jcacheref.prototype.commfault;

import java.io.Serializable;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.jcacheref.prototype.BasicCache;

/**
 * @author Szymon Halastra
 */
@Configuration
public class CommFaultTagCacheConfig implements BasicCache, Serializable {

  private static final String COMM_FAULT_TAG_CACHE = "commFaultTagCache";

  @Bean(name = COMM_FAULT_TAG_CACHE)
  public Cache<Long, CommFaultTag> getCommFaultTagCache(JCacheCacheManager cacheManager) {
    CacheManager cm = cacheManager.getCacheManager();
    return cm.getCache(COMM_FAULT_TAG_CACHE, Long.class, CommFaultTag.class);
  }

  @Override
  public C2monCacheName getName() {
    return C2monCacheName.COMMFAULT;
  }
}
