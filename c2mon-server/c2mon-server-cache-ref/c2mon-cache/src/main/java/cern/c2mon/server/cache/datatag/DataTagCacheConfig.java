package cern.c2mon.server.cache.datatag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.loader.DataTagLoaderDAO;
import cern.c2mon.server.cache.loader.common.BatchCacheLoader;
import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;
import cern.c2mon.server.common.datatag.DataTag;

/**
 * @author Szymon Halastra
 */

@Configuration
public class DataTagCacheConfig {

  @Autowired
  ThreadPoolTaskExecutor cacheLoaderTaskExecutor;

  @Autowired
  CacheLoaderProperties properties;

  @Bean(name = CacheName.Names.DATATAG)
  public Cache createCache(AbstractCacheFactory cachingFactory, DataTagLoaderDAO dataTagLoaderDAO) {
    Cache cache = cachingFactory.createCache(CacheName.DATATAG.getLabel(), Long.class, DataTag.class);

    CacheLoader cacheLoader = new BatchCacheLoader<Long, DataTag>(cacheLoaderTaskExecutor, cache, dataTagLoaderDAO,
            properties.getBatchSize(), "DataTagLoader-");
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
