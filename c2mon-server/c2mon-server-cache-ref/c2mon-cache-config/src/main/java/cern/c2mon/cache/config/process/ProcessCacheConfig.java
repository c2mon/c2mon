package cern.c2mon.cache.config.process;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.config.CacheName;
import cern.c2mon.cache.config.config.AbstractSimpleCacheConfig;
import cern.c2mon.server.cache.loader.CacheLoaderDAO;
import cern.c2mon.server.common.process.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Configuration
public class ProcessCacheConfig extends AbstractSimpleCacheConfig<Process> {

  @Autowired
  public ProcessCacheConfig(AbstractCacheFactory cachingFactory, CacheLoaderDAO<Process> cacheLoaderDAORef) {
    super(cachingFactory, CacheName.PROCESS, Process.class, cacheLoaderDAORef);
  }

  @Bean(name = CacheName.Names.PROCESS)
  @Override
  public C2monCache<Process> createCache() {
    return super.createCache();
  }
}
