package cern.c2mon.server.cache.config.inmemory;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.CommFaultTagDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class CommFaultTagCacheConfig {

  @Bean
  public Ehcache commFaultTagEhcache(){
    return new InMemoryCache("commFaultTagCache");
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
