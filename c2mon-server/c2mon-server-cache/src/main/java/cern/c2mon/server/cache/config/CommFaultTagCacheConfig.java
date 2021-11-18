package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.CommFaultTagDAO;
import cern.c2mon.server.ehcache.CacheManager;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.CacheFactory;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class CommFaultTagCacheConfig {

  @Bean
  public CacheFactory commFaultTagEhcache(CacheManager cacheManager) {
    CacheFactory factory = new CacheFactory();
    factory.setCacheName("commFaultTagCache");
    factory.setCacheManager(cacheManager);
    return factory;
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
