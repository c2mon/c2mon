package cern.c2mon.server.cache.rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;

/**
 * @author Szymon Halastra
 */
@Configuration
public class RuleTagCacheConfig {

  @Autowired
  private ThreadPoolTaskExecutor cacheLoaderTaskExecutor;

  @Autowired
  private CacheLoaderProperties properties;

//  @Bean(name = CacheName.Names.RULE)
//  public C2monCache createCache(AbstractCacheFactory cachingFactory, RuleTagLoaderDAO ruleTagLoaderDAO) {
//    C2monCache cache = cachingFactory.createCache(CacheName.RULETAG.getLabel(), Long.class, RuleTag.class);
//
//    CacheLoader cacheLoader = new BatchCacheLoader<Long, RuleTag>(cacheLoaderTaskExecutor, cache, ruleTagLoaderDAO,
//            properties.getBatchSize(), "RuleTagLoader-");
//    cache.setCacheLoader(cacheLoader);
//
//    return cache;
//  }
}
