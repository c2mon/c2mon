package cern.c2mon.server.jcacheref.prototype.command;

import java.io.Serializable;
import java.util.Map;

import javax.cache.configuration.Factory;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;

import cern.c2mon.shared.common.command.CommandTag;

/**
 * @author Szymon Halastra
 */

public class CommandTagLoaderFactory implements Factory<CacheLoader<String, CommandTag>>, Serializable {

  @Override
  public CacheLoader<String, CommandTag> create() {
    return new CacheLoader<String, CommandTag>() {
      @Override
      public CommandTag load(String key) throws CacheLoaderException {
        return null;
      }

      @Override
      public Map<String, CommandTag> loadAll(Iterable<? extends String> keys) throws CacheLoaderException {
        return null;
      }
    };
  }
}
