package cern.c2mon.server.cache.config.ignite;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.CommFaultTagDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.common.commfault.CommFaultTagCacheObject;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class CommFaultTagCacheConfig {

  @Autowired
  private IgniteCacheProperties igniteCacheProperties;

  @Bean
  public Ehcache commFaultTagEhcache(){
    CacheConfiguration cacheCfg = new CacheConfiguration();
    cacheCfg.setIndexedTypes(Long.class, CommFaultTagCacheObject.class);
    return new IgniteCacheImpl("commFaultTagCache", igniteCacheProperties, cacheCfg);
  }

  @Bean
  public EhcacheLoaderImpl commFaultTagEhcacheLoader(Ehcache commFaultTagEhcache, CommFaultTagDAO commFaultTagDAO) {
    return new EhcacheLoaderImpl<>(commFaultTagEhcache, commFaultTagDAO);
  }

  @Bean
  public C2monCacheLoader commFaultTagCacheLoader(Ehcache commFaultTagEhcache, CommFaultTagDAO commFaultTagDAO) {
    return new SimpleC2monCacheLoader<>(commFaultTagEhcache, commFaultTagDAO);
  }
}
