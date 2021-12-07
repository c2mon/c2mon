package cern.c2mon.server.cache.config.ignite;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.process.query.ProcessIgniteQuery;
import cern.c2mon.server.cache.process.query.ProcessQuery;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class ProcessCacheConfig {

  @Autowired
  private IgniteCacheProperties igniteCacheProperties;

  @Bean
  public Ehcache processEhcache(){
    return new IgniteCacheImpl("processCache", igniteCacheProperties);
  }

  @Bean
  public EhcacheLoaderImpl processEhcacheLoader(Ehcache processEhcache, ProcessDAO processDAO) {
    return new EhcacheLoaderImpl<>(processEhcache, processDAO);
  }

  @Bean
  public ProcessQuery processQuery(Ehcache processEhcache){
    return new ProcessIgniteQuery(processEhcache);
  }

  @Bean
  public C2monCacheLoader processCacheLoader(Ehcache processEhcache, ProcessDAO processDAO) {
    return new SimpleC2monCacheLoader<>(processEhcache, processDAO);
  }
}
