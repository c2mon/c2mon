package cern.c2mon.server.cache.config.inmemory;

import cern.c2mon.server.cache.datatag.query.DataTagInMemoryQuery;
import cern.c2mon.server.cache.datatag.query.DataTagQuery;
import cern.c2mon.server.cache.loading.DataTagLoaderDAO;
import cern.c2mon.server.cache.loading.common.BatchCacheLoader;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.loading.config.CacheLoadingProperties;
import cern.c2mon.server.cache.tag.query.TagInMemoryQuery;
import cern.c2mon.server.cache.tag.query.TagQuery;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class DataTagCacheConfig {

  @Autowired
  private CacheLoadingProperties properties;

  @Bean
  public Ehcache dataTagEhcache(){
    return new InMemoryCache("dataTagCache");
  }

  @Bean
  public EhcacheLoaderImpl dataTagEhcacheLoader(Ehcache dataTagEhcache, DataTagLoaderDAO dataTagLoaderDAO) {
    return new EhcacheLoaderImpl<>(dataTagEhcache, dataTagLoaderDAO);
  }

  @Bean
  public DataTagQuery dataTagQuery(Ehcache dataTagEhcache){
    return new DataTagInMemoryQuery(dataTagEhcache);
  }

  @Bean
  public TagQuery abstractDataTagQuery(Ehcache dataTagEhcache){
    return new TagInMemoryQuery<DataTag>(dataTagEhcache);
  }

  @Bean
  public C2monCacheLoader dataTagCacheLoader(Ehcache dataTagEhcache, DataTagLoaderDAO dataTagLoaderDAO) {
    Integer batchSize = properties.getBatchSize();
    return new BatchCacheLoader<>(dataTagEhcache, dataTagLoaderDAO, batchSize, "DataTagCacheLoader-");
  }
}
