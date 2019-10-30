package cern.c2mon.cache.config.alivetimer;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.config.CacheName;
import cern.c2mon.cache.config.config.AbstractSimpleCacheConfig;
import cern.c2mon.server.cache.loader.CacheLoaderDAO;
import cern.c2mon.server.common.alive.AliveTimer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Configuration
public class AliveTimerCacheConfig extends AbstractSimpleCacheConfig<AliveTimer> {

  @Inject
  public AliveTimerCacheConfig(AbstractCacheFactory cachingFactory, CacheLoaderDAO<AliveTimer> cacheLoaderDAORef) {
    super(cachingFactory, CacheName.ALIVETIMER, AliveTimer.class, cacheLoaderDAORef);
  }

  @Bean(name = CacheName.Names.ALIVETIMER)
  @Override
  public C2monCache<AliveTimer> createCache() {
    return super.createCache();
  }
}