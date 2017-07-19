package cern.c2mon.server.jcacheref.prototype.rule;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.jcacheref.prototype.common.BasicCache;

/**
 * @author Szymon Halastra
 */

@Configuration
public class RuleTagCacheConfig implements BasicCache {

  private static final String RULE_TAG_CACHE = "ruleTagCacheRef";

  @Bean(name = RULE_TAG_CACHE)
  public Cache<Long, RuleTag> getRuleTagCache() {
//    CacheManager cm = cacheManager.getCacheManager();
//    return cm.getCache(RULE_TAG_CACHE, Long.class, RuleTag.class);

    return null;
  }

  @Override
  public C2monCacheName getName() {
    return C2monCacheName.RULETAG;
  }
}
