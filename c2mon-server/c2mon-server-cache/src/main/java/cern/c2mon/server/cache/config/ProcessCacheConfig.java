package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.ehcache.CacheManager;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.CacheFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class ProcessCacheConfig {

  @Bean
  public CacheFactory processEhcache(CacheManager cacheManager) {
    CacheFactory factory = new CacheFactory();
    factory.setCacheName("processCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }

  @Bean
  public EhcacheLoaderImpl processEhcacheLoader(Ehcache processEhcache, ProcessDAO processDAO) {
    return new EhcacheLoaderImpl<>(processEhcache, processDAO);
  }

  @Bean
  public C2monCacheLoader processCacheLoader(Ehcache processEhcache, ProcessDAO processDAO) {
    return new SimpleC2monCacheLoader<>(processEhcache, processDAO);
  }
}
