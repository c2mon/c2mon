package cern.c2mon.cache.config.datatag;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.config.CacheName;
import cern.c2mon.cache.config.config.AbstractBatchCacheConfig;
import cern.c2mon.server.cache.loading.BatchCacheLoaderDAO;
import cern.c2mon.server.cache.loading.config.CacheLoadingProperties;
import cern.c2mon.server.common.datatag.DataTag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.inject.Inject;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Configuration
public class DataTagCacheConfig extends AbstractBatchCacheConfig<DataTag> {

  @Inject
  public DataTagCacheConfig(AbstractCacheFactory cachingFactory, ThreadPoolTaskExecutor cacheLoaderTaskExecutor, CacheLoadingProperties properties, BatchCacheLoaderDAO<DataTag> batchCacheLoaderDAORef) {
    super(cachingFactory, CacheName.DATATAG, DataTag.class, cacheLoaderTaskExecutor, properties, batchCacheLoaderDAORef);
  }

  @Bean(name = CacheName.Names.DATATAG)
  @Override
  public C2monCache<DataTag> createCache() {
    return super.createCache();
  }
}
