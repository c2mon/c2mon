package cern.c2mon.server.cache.config.ignite;

import cern.c2mon.server.cache.datatag.query.DataTagIgniteQuery;
import cern.c2mon.server.cache.datatag.query.DataTagQuery;
import cern.c2mon.server.cache.loading.DataTagLoaderDAO;
import cern.c2mon.server.cache.loading.common.BatchCacheLoader;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.loading.config.CacheLoadingProperties;
import cern.c2mon.server.cache.tag.query.TagIgniteQuery;
import cern.c2mon.server.cache.tag.query.TagQuery;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class DataTagCacheConfig {

  @Autowired
  private CacheLoadingProperties properties;

  @Autowired
  private IgniteCacheProperties igniteCacheProperties;

  @Bean
  public Ehcache dataTagEhcache(){
    return new IgniteCacheImpl("dataTagCache", igniteCacheProperties);
  }

  @Bean
  public EhcacheLoaderImpl dataTagEhcacheLoader(Ehcache dataTagEhcache, DataTagLoaderDAO dataTagLoaderDAO) {
    return new EhcacheLoaderImpl<>(dataTagEhcache, dataTagLoaderDAO);
  }

  @Bean
  public DataTagQuery dataTagQuery(Ehcache dataTagEhcache){
    return new DataTagIgniteQuery(dataTagEhcache);
  }

  @Bean
  public TagQuery abstractDataTagQuery(Ehcache dataTagEhcache){
    return new TagIgniteQuery<DataTag>(dataTagEhcache);
  }

  @Bean
  public C2monCacheLoader dataTagCacheLoader(Ehcache dataTagEhcache, DataTagLoaderDAO dataTagLoaderDAO) {
    Integer batchSize = properties.getBatchSize();
    return new BatchCacheLoader<>(dataTagEhcache, dataTagLoaderDAO, batchSize, "DataTagCacheLoader-");
  }
}
