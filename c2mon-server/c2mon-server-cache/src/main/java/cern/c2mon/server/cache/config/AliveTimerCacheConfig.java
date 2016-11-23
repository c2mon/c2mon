package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.AliveTimerDAO;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class AliveTimerCacheConfig {

  @Bean
  public EhCacheFactoryBean aliveTimerEhcache(CacheManager cacheManager) {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
    factory.setCacheName("aliveTimerCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }

  @Bean
  public EhcacheLoaderImpl aliveTimerEhcacheLoader(Ehcache aliveTimerEhcache, AliveTimerDAO aliveTimerDAO) {
    return new EhcacheLoaderImpl<>(aliveTimerEhcache, aliveTimerDAO);
  }

  @Bean
  public C2monCacheLoader aliveTimerCacheLoader(Ehcache aliveTimerEhcache, AliveTimerDAO aliveTimerDAO) {
    return new SimpleC2monCacheLoader<>(aliveTimerEhcache, aliveTimerDAO);
  }
}
