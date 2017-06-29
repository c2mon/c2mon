package cern.c2mon.server.jcacheref.various;

import javax.cache.event.*;

import cern.c2mon.server.common.tag.Tag;

/**
 * @author Szymon Halastra
 */
public class EventListener implements
        CacheEntryCreatedListener<Long, Tag>,
        CacheEntryUpdatedListener<Long, Tag>,
        CacheEntryRemovedListener<Long, Tag> {

  @Override
  public void onCreated(Iterable<CacheEntryEvent<? extends Long, ? extends Tag>> cacheEntryEvents) throws CacheEntryListenerException {

  }

  @Override
  public void onUpdated(Iterable<CacheEntryEvent<? extends Long, ? extends Tag>> cacheEntryEvents) throws CacheEntryListenerException {

  }

  @Override
  public void onRemoved(Iterable<CacheEntryEvent<? extends Long, ? extends Tag>> cacheEntryEvents) throws CacheEntryListenerException {

  }
}
