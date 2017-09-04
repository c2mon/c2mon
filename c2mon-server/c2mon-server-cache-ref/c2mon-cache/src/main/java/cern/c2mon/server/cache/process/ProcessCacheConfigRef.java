package cern.c2mon.server.cache.process;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.common.process.Process;

/**
 * @author Szymon Halastra
 */
@Configuration
public class ProcessCacheConfigRef {

  @Bean(name = C2monCacheName.Names.PROCESS)
  public C2monCache createCache(AbstractC2monCacheFactory cachingFactory) {
    return cachingFactory.createCache(C2monCacheName.PROCESS.getLabel(), Long.class, Process.class);
  }
}
