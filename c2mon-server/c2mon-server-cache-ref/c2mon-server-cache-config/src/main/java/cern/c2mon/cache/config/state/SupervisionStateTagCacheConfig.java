package cern.c2mon.cache.config.state;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.config.CacheName;
import cern.c2mon.cache.config.config.AbstractSimpleCacheConfig;
import cern.c2mon.server.cache.loading.CacheLoaderDAO;
import cern.c2mon.server.common.status.SupervisionStateTag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

/**
 * @author Alexandros Papageorgiou Koufidis
 */
@Configuration
public class SupervisionStateTagCacheConfig extends AbstractSimpleCacheConfig<SupervisionStateTag> {

  @Inject
  public SupervisionStateTagCacheConfig(AbstractCacheFactory cachingFactory, CacheLoaderDAO<SupervisionStateTag> cacheLoaderDAORef) {
    super(cachingFactory, CacheName.STATETAG, SupervisionStateTag.class, cacheLoaderDAORef);
  }

  @Bean(name = CacheName.Names.STATETAG)
  @Override
  public C2monCache<SupervisionStateTag> createCache() {
    return super.createCache();
  }
}
