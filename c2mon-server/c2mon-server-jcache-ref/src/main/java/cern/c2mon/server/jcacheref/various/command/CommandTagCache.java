package cern.c2mon.server.jcacheref.various.command;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.*;

import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.shared.common.command.CommandTag;

/**
 * @author Szymon Halastra
 */
public class CommandTagCache {

  private static final String CACHE_NAME = "command-tag-cache";
  private Cache<Long, CommandTag> cache;
  private MutableConfiguration<Long, CommandTag> configuration;

  @Autowired
  public CommandTagCache(final CacheManager cacheManager) {
    configuration = new MutableConfiguration<>();

    configuration.setTypes(Long.class, CommandTag.class);

    cache = cacheManager.createCache(CACHE_NAME, configuration);
  }
}
