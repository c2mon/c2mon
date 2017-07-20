package cern.c2mon.server.jcacheref.prototype.command;

import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.stereotype.Component;

import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.jcacheref.prototype.common.AbstractCacheRef;
import cern.c2mon.server.jcacheref.prototype.common.BasicCache;
import cern.c2mon.shared.common.command.CommandTag;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Component
public class CommandTagCacheRef extends AbstractCacheRef<Long, CommandTag> implements BasicCache, Serializable {

  private static final String COMMAND_TAG_CACHE = "commandTagCache";

  public CommandTagCacheRef() {
    super();
  }

  @Override
  public C2monCacheName getName() {
    return C2monCacheName.COMMAND;
  }

  @Override
  protected CacheConfiguration configureCache() {
    CacheConfiguration<Long, CommandTag> config = new CacheConfiguration<>(COMMAND_TAG_CACHE);

    config.setIndexedTypes(Long.class, CommandTag.class);
    config.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);

    return config;
  }
}
