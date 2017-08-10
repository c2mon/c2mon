package cern.c2mon.server.cache.commfault;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.factory.AbstractFactory;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;

/**
 * @author Szymon Halastra
 */
@Configuration
public class CommFaultTagCacheConfig {

  public static final String COMM_FAULT_CACHE = "commFaultCache";

  @Bean(name = COMM_FAULT_CACHE)
  public C2monCache createCache(AbstractFactory cachingFactory) {
    return cachingFactory.createCache(COMM_FAULT_CACHE, Long.class, CommFaultTag.class);
  }
}
