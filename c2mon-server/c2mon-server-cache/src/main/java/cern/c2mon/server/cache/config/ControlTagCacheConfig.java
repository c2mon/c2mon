package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.ControlTagLoaderDAO;
import cern.c2mon.server.ehcache.CacheManager;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.CacheFactory;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class ControlTagCacheConfig {

  @Bean
  public CacheFactory controlTagEhcache(CacheManager cacheManager) {
    CacheFactory factory = new CacheFactory();
    factory.setCacheName("controlCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }

  @Bean
  public EhcacheLoaderImpl controlTagEhcacheLoader(Ehcache controlTagEhcache, ControlTagLoaderDAO controlTagLoaderDAO) {
    return new EhcacheLoaderImpl<>(controlTagEhcache, controlTagLoaderDAO);
  }

  @Bean
  public C2monCacheLoader controlTagCacheLoader(Ehcache controlTagEhcache, ControlTagLoaderDAO controlTagLoaderDAO) {
    return new SimpleC2monCacheLoader<>(controlTagEhcache, controlTagLoaderDAO);
  }
}
