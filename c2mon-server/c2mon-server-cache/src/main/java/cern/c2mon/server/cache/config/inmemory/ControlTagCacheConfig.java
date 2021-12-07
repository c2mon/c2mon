package cern.c2mon.server.cache.config.inmemory;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.ControlTagLoaderDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.tag.query.TagInMemoryQuery;
import cern.c2mon.server.cache.tag.query.TagQuery;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class ControlTagCacheConfig {

  @Bean
  public Ehcache controlTagEhcache(){
    return new InMemoryCache("controlTagCache");
  }

  @Bean
  public EhcacheLoaderImpl controlTagEhcacheLoader(Ehcache controlTagEhcache, ControlTagLoaderDAO controlTagLoaderDAO) {
    return new EhcacheLoaderImpl<>(controlTagEhcache, controlTagLoaderDAO);
  }

  @Bean
  public C2monCacheLoader controlTagCacheLoader(Ehcache controlTagEhcache, ControlTagLoaderDAO controlTagLoaderDAO) {
    return new SimpleC2monCacheLoader<>(controlTagEhcache, controlTagLoaderDAO);
  }

  @Bean
  public TagQuery abstractControlTagQuery(Ehcache controlTagEhcache){
    return new TagInMemoryQuery<ControlTag>(controlTagEhcache);
  }

}
