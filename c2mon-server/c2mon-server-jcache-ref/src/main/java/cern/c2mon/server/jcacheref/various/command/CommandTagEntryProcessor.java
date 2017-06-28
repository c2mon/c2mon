package cern.c2mon.server.jcacheref.various.command;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;

import cern.c2mon.shared.common.command.CommandTag;

/**
 * @author Szymon Halastra
 */
public class CommandTagEntryProcessor implements EntryProcessor<Long, CommandTag, CommandTag> {

  @Override
  public CommandTag process(MutableEntry<Long, CommandTag> entry, Object... arguments) throws EntryProcessorException {
    return null;
  }
}
