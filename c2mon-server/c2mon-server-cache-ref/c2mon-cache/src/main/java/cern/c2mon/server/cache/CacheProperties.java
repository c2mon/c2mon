package cern.c2mon.server.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.cache.impl.IgniteFactory;

/**
 * @author Szymon Halastra
 */

@Configuration
@ComponentScan(value = "cern.c2mon.cache.impl")
public class CacheProperties {

  @Bean(name = "cachingFactory")
  public AbstractC2monCacheFactory getCacheFactory() {
    return new IgniteFactory();
  }
}
