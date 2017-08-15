package cern.c2mon.server.cache.rule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractFactory;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.common.rule.RuleTag;

/**
 * @author Szymon Halastra
 */
@Configuration
public class RuleTagCacheConfig {

  @Bean(name = C2monCacheName.Names.RULE)
  public C2monCache createCache(AbstractFactory cachingFactory) {
    return cachingFactory.createCache(C2monCacheName.RULETAG.getLabel(), Long.class, RuleTag.class);
  }
}
