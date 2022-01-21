package cern.c2mon.server.cache.config.ignite;

import cern.c2mon.server.cache.loading.RuleTagLoaderDAO;
import cern.c2mon.server.cache.loading.common.BatchCacheLoader;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.loading.config.CacheLoadingProperties;
import cern.c2mon.server.cache.tag.query.TagIgniteQuery;
import cern.c2mon.server.cache.tag.query.TagQuery;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class RuleTagCacheConfig {

  @Autowired
  private CacheLoadingProperties properties;

  @Autowired
  private IgniteCacheProperties igniteCacheProperties;

  @Bean
  public Ehcache ruleTagEhcache(){
    CacheConfiguration cacheCfg = new CacheConfiguration();
    cacheCfg.setIndexedTypes(Long.class, RuleTagCacheObject.class);
    return new IgniteCacheImpl("ruleTagCache", igniteCacheProperties, cacheCfg);
  }

  @Bean
  public EhcacheLoaderImpl ruleTagEhcacheLoader(Ehcache ruleTagEhcache, RuleTagLoaderDAO ruleTagLoaderDAO) {
    return new EhcacheLoaderImpl<>(ruleTagEhcache, ruleTagLoaderDAO);
  }

  @Bean
  public C2monCacheLoader ruleTagCacheLoader(Ehcache ruleTagEhcache, RuleTagLoaderDAO ruleTagLoaderDAO) {
    Integer batchSize = properties.getBatchSize();
    return new BatchCacheLoader<>(ruleTagEhcache, ruleTagLoaderDAO, batchSize, "RuleTagCacheLoader-");
  }

  @Bean
  public TagQuery abstractRuleTagQuery(Ehcache ruleTagEhcache){
    return new TagIgniteQuery<RuleTag>(ruleTagEhcache);
  }
}
