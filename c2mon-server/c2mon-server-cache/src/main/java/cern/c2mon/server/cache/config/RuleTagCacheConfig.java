package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.loading.common.BatchCacheLoader;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.loading.RuleTagLoaderDAO;
import cern.c2mon.server.cache.loading.config.CacheLoadingProperties;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class RuleTagCacheConfig {

  @Autowired
  private CacheLoadingProperties properties;

  @Bean
  public EhCacheFactoryBean ruleTagEhcache(CacheManager cacheManager) {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
    factory.setCacheName("ruleCache");
    factory.setCacheManager(cacheManager);
    return factory;
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
}
