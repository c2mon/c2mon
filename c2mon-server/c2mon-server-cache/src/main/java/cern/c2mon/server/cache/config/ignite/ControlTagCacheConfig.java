package cern.c2mon.server.cache.config.ignite;

import cern.c2mon.server.cache.common.SimpleC2monCacheLoader;
import cern.c2mon.server.cache.loading.ControlTagLoaderDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.tag.query.TagIgniteQuery;
import cern.c2mon.server.cache.tag.query.TagQuery;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class ControlTagCacheConfig {

  @Autowired
  private IgniteCacheProperties igniteCacheProperties;

  @Bean
  public Ehcache controlTagEhcache(){
    CacheConfiguration cacheCfg = new CacheConfiguration();
    cacheCfg.setIndexedTypes(Long.class, ControlTagCacheObject.class);
    return new IgniteCacheImpl("controlTagCache", igniteCacheProperties, cacheCfg);
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
    return new TagIgniteQuery<ControlTag>(controlTagEhcache);
  }

}
