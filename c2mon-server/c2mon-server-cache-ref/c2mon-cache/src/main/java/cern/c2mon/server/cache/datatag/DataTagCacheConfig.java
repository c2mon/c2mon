package cern.c2mon.server.cache.datatag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.C2monCacheName;
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

  @Bean(name = C2monCacheName.Names.DATATAG)
  public C2monCache createCache(AbstractC2monCacheFactory cachingFactory, DataTagLoaderDAO dataTagLoaderDAO) {
    C2monCache cache = cachingFactory.createCache(C2monCacheName.DATATAG.getLabel(), Long.class, DataTag.class);

    C2monCacheLoader cacheLoader = new BatchCacheLoader<Long, DataTag>(cacheLoaderTaskExecutor, cache, dataTagLoaderDAO,
            properties.getBatchSize(), "DataTagLoader-");
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
