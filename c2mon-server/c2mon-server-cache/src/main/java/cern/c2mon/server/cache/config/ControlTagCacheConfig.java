package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.ControlTagLoaderDAO;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class ControlTagCacheConfig {

  @Bean
  public EhCacheFactoryBean controlTagEhcache(CacheManager cacheManager) {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
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
