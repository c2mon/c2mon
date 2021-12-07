package cern.c2mon.server.cache.config.inmemory;

import cern.c2mon.server.cache.command.query.CommandTagInMemoryQuery;
import cern.c2mon.server.cache.command.query.CommandTagQuery;
import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.CommandTagDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class CommandTagCacheConfig {

  @Bean
  public Ehcache commandTagEhcache(){
    return new InMemoryCache("commandTagCache");
  }

  @Bean
  public EhcacheLoaderImpl commandTagEhcacheLoader(Ehcache commandTagEhcache, CommandTagDAO commandTagLoaderDAO) {
    return new EhcacheLoaderImpl<>(commandTagEhcache, commandTagLoaderDAO);
  }

  @Bean
  public CommandTagQuery commandTagQuery(Ehcache commandTagEhcache){
    return new CommandTagInMemoryQuery(commandTagEhcache);
  }

  @Bean
  public C2monCacheLoader commandTagCacheLoader(Ehcache commandTagEhcache, CommandTagDAO commandTagLoaderDAO) {
    return new SimpleC2monCacheLoader<>(commandTagEhcache, commandTagLoaderDAO);
  }
}
