package cern.c2mon.server.jcacheref.prototype.process;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.jcacheref.prototype.common.BasicCache;

/**
 * @author Szymon Halastra
 */

@Configuration
public class ProcessCacheConfigRef implements BasicCache {

  private static final String PROCESS_CACHE = "processCacheRef";

  @Bean(name = PROCESS_CACHE)
  public Cache<Long, Process> getProcessCache() {
//    CacheManager cm = cacheManager.getCacheManager();
//    return cm.getCache(PROCESS_CACHE, Long.class, Process.class);
    return null;
  }

  @Override
  public C2monCacheName getName() {
    return C2monCacheName.PROCESS;
  }
}
