package cern.c2mon.server.cache.tag;

import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */
@Configuration
public class TagCacheConfig {

  // TODO (Alex): Does this even have a reason for existing? We don't seem to use the TAG cache anywhere?

//  @Bean(name = CacheName.Names.TAG)
//  public C2monCache createCache(AbstractCacheFactory cachingFactory) {
//    //TODO: temporary it has null in place of SingleEntryLoader
//    return cachingFactory.createCache(CacheName.TAG.getLabel(), Long.class, Tag.class);
//  }
}
