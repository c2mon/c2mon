package cern.c2mon.server.cache.rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.loader.RuleTagLoaderDAO;
import cern.c2mon.server.cache.loader.common.BatchCacheLoader;
import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;
import cern.c2mon.server.common.rule.RuleTag;

/**
 * @author Szymon Halastra
 */
@Configuration
public class RuleTagCacheConfig {

  @Autowired
  private ThreadPoolTaskExecutor cacheLoaderTaskExecutor;

  @Autowired
  private CacheLoaderProperties properties;

  @Bean(name = CacheName.Names.RULE)
  public Cache createCache(AbstractCacheFactory cachingFactory, RuleTagLoaderDAO ruleTagLoaderDAO) {
    Cache cache = cachingFactory.createCache(CacheName.RULETAG.getLabel(), Long.class, RuleTag.class);

    C2monCacheLoader cacheLoader = new BatchCacheLoader<Long, RuleTag>(cacheLoaderTaskExecutor, cache, ruleTagLoaderDAO,
            properties.getBatchSize(), "RuleTagLoader-");
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
