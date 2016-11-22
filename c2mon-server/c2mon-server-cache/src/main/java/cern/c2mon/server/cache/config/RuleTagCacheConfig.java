package cern.c2mon.server.cache.config;

import cern.c2mon.server.cache.common.BatchCacheLoader;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.common.EhcacheLoaderImpl;
import cern.c2mon.server.cache.loading.RuleTagLoaderDAO;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class RuleTagCacheConfig {

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
  public C2monCacheLoader ruleTagCacheLoader(Ehcache ruleTagEhcache, RuleTagLoaderDAO ruleTagLoaderDAO, Environment environment) {
    Integer batchSize = environment.getRequiredProperty("c2mon.server.cacheloading.batchSize", Integer.class);
    return new BatchCacheLoader<>(ruleTagEhcache, ruleTagLoaderDAO, batchSize, "RuleTagCacheLoader-");
  }
}
