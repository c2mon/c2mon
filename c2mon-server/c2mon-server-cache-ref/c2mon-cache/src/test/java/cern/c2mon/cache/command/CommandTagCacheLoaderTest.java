package cern.c2mon.cache.command;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.CommandTagMapper;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.test.CacheObjectComparison;
import cern.c2mon.shared.common.command.CommandTag;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class CommandTagCacheLoaderTest extends AbstractCacheLoaderTest<CommandTag> {

  @Autowired
  private C2monCache<CommandTag> commandTagCacheRef;

  @Autowired
  private CommandTagMapper commandTagMapper;

  @Override
  protected LoaderMapper<CommandTag> getMapper() {
    return commandTagMapper;
  }

  @Override
  protected void compareLists(List<CommandTag> mapperList, Map<Long, CommandTag> cacheList) {
    for (CommandTag currentCommandTag : mapperList) {
      CacheObjectComparison.equals((CommandTagCacheObject) currentCommandTag,
        (CommandTagCacheObject) cacheList.get(currentCommandTag.getId()));
    }
  }

  @Override
  protected CommandTag getSample() {
    return new CommandTagCacheObject(0L);
  }

  @Override
  protected Long getExistingKey() {
    return 11000L;
  }

  @Override
  protected C2monCache<CommandTag> getCache() {
    return commandTagCacheRef;
  }
}
