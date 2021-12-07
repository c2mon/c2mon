package cern.c2mon.server.cache.config.inmemory;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.process.query.ProcessInMemoryQuery;
import cern.c2mon.server.cache.process.query.ProcessQuery;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class ProcessCacheConfig {

  @Bean
  public Ehcache processEhcache(){
    return new InMemoryCache("processCache");
  }

  @Bean
  public EhcacheLoaderImpl processEhcacheLoader(Ehcache processEhcache, ProcessDAO processDAO) {
    return new EhcacheLoaderImpl<>(processEhcache, processDAO);
  }

  @Bean
  public ProcessQuery processQuery(Ehcache processEhcache){
    return new ProcessInMemoryQuery(processEhcache);
  }

  @Bean
  public C2monCacheLoader processCacheLoader(Ehcache processEhcache, ProcessDAO processDAO) {
    return new SimpleC2monCacheLoader<>(processEhcache, processDAO);
  }
}
