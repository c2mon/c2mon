package cern.c2mon.server.jcacheref.prototype.command;

import java.io.Serializable;

import javax.cache.Cache;
import javax.cache.CacheManager;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.jcacheref.prototype.common.BasicCache;
import cern.c2mon.shared.common.command.CommandTag;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Component
public class CommandTagCacheConfig implements BasicCache, Serializable {

  private static final String COMMAND_TAG_CACHE = "commandTagCache";

  @Bean(name = COMMAND_TAG_CACHE)
  public Cache<Long, CommandTag> getCommandTagCache() {
//    CacheManager cm = cacheManager.getCacheManager();
//    return cm.getCache(COMMAND_TAG_CACHE, Long.class, CommandTag.class);

    return null;
  }

  @Override
  public C2monCacheName getName() {
    return C2monCacheName.COMMAND;
  }
}
