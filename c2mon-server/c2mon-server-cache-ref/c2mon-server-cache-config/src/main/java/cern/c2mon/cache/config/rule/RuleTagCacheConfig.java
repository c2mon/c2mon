package cern.c2mon.cache.config.rule;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.config.CacheName;
import cern.c2mon.cache.config.config.AbstractBatchCacheConfig;
import cern.c2mon.server.cache.loading.BatchCacheLoaderDAO;
import cern.c2mon.server.cache.loading.config.CacheLoadingProperties;
import cern.c2mon.server.common.rule.RuleTag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.inject.Inject;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Configuration
public class RuleTagCacheConfig extends AbstractBatchCacheConfig<RuleTag> {

  @Inject
  public RuleTagCacheConfig(AbstractCacheFactory cachingFactory, ThreadPoolTaskExecutor cacheLoaderTaskExecutor, CacheLoadingProperties properties, BatchCacheLoaderDAO<RuleTag> batchCacheLoaderDAORef) {
    super(cachingFactory, CacheName.RULETAG, RuleTag.class, cacheLoaderTaskExecutor, properties, batchCacheLoaderDAORef);
  }

  @Bean(name = CacheName.Names.RULETAG)
  @Override
  public C2monCache<RuleTag> createCache() {
    return super.createCache();
  }
}
