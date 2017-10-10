package cern.c2mon.server.cache.tag;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.common.tag.Tag;

/**
 * @author Szymon Halastra
 */
@Configuration
public class TagCacheConfig {

  @Bean(name = CacheName.Names.TAG)
  public Cache createCache(AbstractCacheFactory cachingFactory) {
    //TODO: temporary it has null in place of SingleEntryLoader
    return cachingFactory.createCache(CacheName.TAG.getLabel(), Long.class, Tag.class);
  }
}
