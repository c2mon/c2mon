package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.ProcessDAO;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class ProcessCacheConfig {

  @Bean
  public EhCacheFactoryBean processEhcache(CacheManager cacheManager) {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
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
