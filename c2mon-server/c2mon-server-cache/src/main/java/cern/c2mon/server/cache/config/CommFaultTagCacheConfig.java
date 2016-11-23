package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.CommFaultTagDAO;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class CommFaultTagCacheConfig {

  @Bean
  public EhCacheFactoryBean commFaultTagEhcache(CacheManager cacheManager) {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
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
