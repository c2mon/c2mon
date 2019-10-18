package cern.c2mon.cache.config.control;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.config.CacheName;
import cern.c2mon.cache.config.config.AbstractSimpleCacheConfig;
import cern.c2mon.server.cache.loader.CacheLoaderDAO;
import cern.c2mon.server.common.control.ControlTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */

@Configuration
public class ControlTagCacheConfig extends AbstractSimpleCacheConfig<ControlTag> {

  @Autowired
  protected ControlTagCacheConfig(AbstractCacheFactory cachingFactory, CacheLoaderDAO<ControlTag> cacheLoaderDAORef) {
    super(cachingFactory, CacheName.CONTROLTAG, ControlTag.class, cacheLoaderDAORef);
  }

  @Bean(name = CacheName.Names.CONTROLTAG)
  @Override
  public C2monCache<ControlTag> createCache() {
    return super.createCache();
  }
}
