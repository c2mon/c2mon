package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.CommandTagDAO;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class CommandTagCacheConfig {

  @Bean
  public EhCacheFactoryBean commandTagEhcache(CacheManager cacheManager) {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
    factory.setCacheName("commandCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }

  @Bean
  public EhcacheLoaderImpl commandTagEhcacheLoader(Ehcache commandTagEhcache, CommandTagDAO commandTagLoaderDAO) {
    return new EhcacheLoaderImpl<>(commandTagEhcache, commandTagLoaderDAO);
  }

  @Bean
  public C2monCacheLoader commandTagCacheLoader(Ehcache commandTagEhcache, CommandTagDAO commandTagLoaderDAO) {
    return new SimpleC2monCacheLoader<>(commandTagEhcache, commandTagLoaderDAO);
  }
}
