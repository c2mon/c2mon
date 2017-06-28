package cern.c2mon.server.jcacheref.prototype.command;

import java.util.Collection;

import javax.cache.Cache;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;

import cern.c2mon.shared.common.command.CommandTag;

/**
 * @author Szymon Halastra
 */
public class CommandTagWriter implements CacheWriter<Long, CommandTag> {

  @Override
  public void write(Cache.Entry<? extends Long, ? extends CommandTag> entry) throws CacheWriterException {

  }

  @Override
  public void writeAll(Collection<Cache.Entry<? extends Long, ? extends CommandTag>> entries) throws CacheWriterException {

  }

  @Override
  public void delete(Object key) throws CacheWriterException {

  }

  @Override
  public void deleteAll(Collection<?> keys) throws CacheWriterException {

  }
}
