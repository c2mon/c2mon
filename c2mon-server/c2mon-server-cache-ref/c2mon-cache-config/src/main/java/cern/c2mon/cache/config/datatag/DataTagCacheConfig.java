package cern.c2mon.cache.config.datatag;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.config.CacheName;
import cern.c2mon.cache.config.config.AbstractBatchCacheConfig;
import cern.c2mon.server.cache.loader.BatchCacheLoaderDAO;
import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;
import cern.c2mon.server.common.datatag.DataTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Configuration
public class DataTagCacheConfig extends AbstractBatchCacheConfig<DataTag> {

  @Autowired
  public DataTagCacheConfig(AbstractCacheFactory cachingFactory, ThreadPoolTaskExecutor cacheLoaderTaskExecutor, CacheLoaderProperties properties, BatchCacheLoaderDAO<DataTag> batchCacheLoaderDAORef) {
    super(cachingFactory, CacheName.DATATAG, DataTag.class, cacheLoaderTaskExecutor, properties, batchCacheLoaderDAORef);
  }

  @Bean(name = CacheName.Names.DATATAG)
  @Override
  public C2monCache<DataTag> createCache() {
    return super.createCache();
  }
}
