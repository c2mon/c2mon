package cern.c2mon.server.cache.datatag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;

/**
 * @author Szymon Halastra
 */

@Configuration
public class DataTagCacheConfig {

  @Autowired
  ThreadPoolTaskExecutor cacheLoaderTaskExecutor;

  @Autowired
  CacheLoaderProperties properties;

//  @Bean(name = CacheName.Names.DATATAG)
//  public C2monCache createCache(AbstractCacheFactory cachingFactory, DataTagLoaderDAO dataTagLoaderDAORef) {
//    C2monCache cache = cachingFactory.createCache(CacheName.DATATAG.getLabel(), Long.class, DataTag.class);
//
//    CacheLoader cacheLoader = new BatchCacheLoader<Long, DataTag>(cacheLoaderTaskExecutor, cache, dataTagLoaderDAORef,
//            properties.getBatchSize(), "DataTagLoader-");
//    cache.setCacheLoader(cacheLoader);
//
//    return cache;
//  }
}
