package cern.c2mon.cache.config.commfault;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.config.CacheName;
import cern.c2mon.cache.config.config.AbstractSimpleCacheConfig;
import cern.c2mon.server.cache.loader.CacheLoaderDAO;
import cern.c2mon.server.common.commfault.CommFaultTag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Configuration
public class CommFaultTagCacheConfig extends AbstractSimpleCacheConfig<CommFaultTag> {

  @Inject
  public CommFaultTagCacheConfig(AbstractCacheFactory cachingFactory, CacheLoaderDAO<CommFaultTag> cacheLoaderDAORef) {
    super(cachingFactory, CacheName.COMMFAULTTAG, CommFaultTag.class, cacheLoaderDAORef);
  }

  @Bean(name = CacheName.Names.COMMFAULTTAG)
  @Override
  public C2monCache<CommFaultTag> createCache() {
    return super.createCache();
  }
}
