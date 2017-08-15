package cern.c2mon.server.cache.command;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractFactory;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.shared.common.command.CommandTag;

/**
 * @author Szymon Halastra
 */
@Configuration
public class CommandTagCacheConfig {

  @Bean(name = C2monCacheName.Names.COMMAND)
  public C2monCache createCache(AbstractFactory cachingFactory) {
    return cachingFactory.createCache(C2monCacheName.COMMAND.getLabel(), Long.class, CommandTag.class);
  }
}
