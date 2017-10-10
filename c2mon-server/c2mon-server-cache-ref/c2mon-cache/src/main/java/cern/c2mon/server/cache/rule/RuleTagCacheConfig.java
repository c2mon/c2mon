package cern.c2mon.server.cache.rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.C2monCacheName;
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

  @Bean(name = C2monCacheName.Names.RULE)
  public Cache createCache(AbstractC2monCacheFactory cachingFactory, RuleTagLoaderDAO ruleTagLoaderDAO) {
    Cache cache = cachingFactory.createCache(C2monCacheName.RULETAG.getLabel(), Long.class, RuleTag.class);

    C2monCacheLoader cacheLoader = new BatchCacheLoader<Long, RuleTag>(cacheLoaderTaskExecutor, cache, ruleTagLoaderDAO,
            properties.getBatchSize(), "RuleTagLoader-");
    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
