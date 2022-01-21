package cern.c2mon.server.cache.config.ignite;

import cern.c2mon.server.cache.command.query.CommandTagIgniteQuery;
import cern.c2mon.server.cache.command.query.CommandTagQuery;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.CommandTagDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class CommandTagCacheConfig {

  @Autowired
  private IgniteCacheProperties igniteCacheProperties;

  @Bean
  public Ehcache commandTagEhcache(){
    CacheConfiguration cacheCfg = new CacheConfiguration();
    cacheCfg.setIndexedTypes(Long.class, CommandTagCacheObject.class);
    return new IgniteCacheImpl("commandTagCache", igniteCacheProperties, cacheCfg);
  }

  @Bean
  public EhcacheLoaderImpl commandTagEhcacheLoader(Ehcache commandTagEhcache, CommandTagDAO commandTagLoaderDAO) {
    return new EhcacheLoaderImpl<>(commandTagEhcache, commandTagLoaderDAO);
  }

  @Bean
  public CommandTagQuery commandTagQuery(Ehcache commandTagEhcache){
    return new CommandTagIgniteQuery(commandTagEhcache);
  }

  @Bean
  public C2monCacheLoader commandTagCacheLoader(Ehcache commandTagEhcache, CommandTagDAO commandTagLoaderDAO) {
    return new SimpleC2monCacheLoader<>(commandTagEhcache, commandTagLoaderDAO);
  }
}
