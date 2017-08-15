package cern.c2mon.server.cache.control;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractFactory;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.common.control.ControlTag;

/**
 * @author Szymon Halastra
 */

@Configuration
public class ControlTagCacheConfig {

  @Bean(name = C2monCacheName.Names.CONTROL)
  public C2monCache createCache(AbstractFactory cachingFactory) {
    return cachingFactory.createCache(C2monCacheName.CONTROLTAG.getLabel(), Long.class, ControlTag.class);
  }
}
