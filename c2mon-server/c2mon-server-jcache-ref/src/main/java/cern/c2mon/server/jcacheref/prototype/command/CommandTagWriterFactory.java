package cern.c2mon.server.jcacheref.prototype.command;

import javax.cache.configuration.Factory;
import javax.cache.integration.CacheWriter;

import cern.c2mon.shared.common.command.CommandTag;

/**
 * @author Szymon Halastra
 */
public class CommandTagWriterFactory implements Factory<CacheWriter<Long, CommandTag>> {

  @Override
  public CacheWriter<Long, CommandTag> create() {
    return new CommandTagWriter();
  }
}
