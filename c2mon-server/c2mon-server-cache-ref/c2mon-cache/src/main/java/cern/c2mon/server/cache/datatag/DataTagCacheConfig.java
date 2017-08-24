package cern.c2mon.server.cache.datatag;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.common.datatag.DataTag;

/**
 * @author Szymon Halastra
 */

@Configuration
public class DataTagCacheConfig {

  @Bean(name = C2monCacheName.Names.DATATAG)
  public C2monCache createCache(AbstractC2monCacheFactory cachingFactory) {
    return cachingFactory.createCache(C2monCacheName.DATATAG.getLabel(), Long.class, DataTag.class);
  }
}
