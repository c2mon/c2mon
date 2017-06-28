package cern.c2mon.server.jcacheref.various.command;

import javax.cache.event.*;

import cern.c2mon.shared.common.command.CommandTag;

/**
 * @author Szymon Halastra
 */
public class CommandTagEntryListeners implements
        CacheEntryCreatedListener<Long, CommandTag>,
        CacheEntryUpdatedListener<Long, CommandTag>,
        CacheEntryRemovedListener<Long, CommandTag> {

  @Override
  public void onCreated(Iterable<CacheEntryEvent<? extends Long, ? extends CommandTag>> cacheEntryEvents) throws CacheEntryListenerException {

  }

  @Override
  public void onRemoved(Iterable<CacheEntryEvent<? extends Long, ? extends CommandTag>> cacheEntryEvents) throws CacheEntryListenerException {

  }

  @Override
  public void onUpdated(Iterable<CacheEntryEvent<? extends Long, ? extends CommandTag>> cacheEntryEvents) throws CacheEntryListenerException {

  }
}
