/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.cache.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;

import org.apache.log4j.Logger;

import cern.c2mon.server.cache.CacheSupervisionListener;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.C2monCacheWithSupervision;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.common.tag.Tag;

/**
 * Common methods used by all tag caches (data, control and rule tags).
 * Objects in these caches are {@link AbstractTagCacheObject}s and implement
 * the {@link Tag} interface.
 * 
 * TODO still need to add listener lifecycle callback if add listeners on new threads (not available so far)
 * 
 * @param <T> cache object type
 * 
 * @author Mark Brightwell
 *
 */
public abstract class AbstractTagCache<T extends Tag> extends AbstractCache<Long, T> implements C2monCacheWithSupervision<Long, T> {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AbstractTagCache.class);
  
  /**
   * Synchronized list.
   */
  private final List<CacheSupervisionListener< ? super T>> listenersWithSupervision;
  private final ReentrantReadWriteLock listenerLock;
  
  /**
   * Constructor.
   */
  public AbstractTagCache(final ClusterCache clusterCache, 
                          final Ehcache ehcache,
                          final CacheLoader cacheLoader, 
                          final C2monCacheLoader c2monCacheLoader,
                          final SimpleCacheLoaderDAO<T> cacheLoaderDAO) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);    
    listenersWithSupervision = new ArrayList<CacheSupervisionListener< ? super T>>();
    listenerLock = new ReentrantReadWriteLock();
  }
  
  
  @Override
  public void notifyListenersOfSupervisionChange(final T tag) {
    //only notify if the current value in the cache is not more recent (a new update could have overraken the supervision notification)
    if (!this.getCopy(tag.getId()).getCacheTimestamp().after(tag.getCacheTimestamp())) {
      notifyListenersWithSupervision(tag);
    } else {
      LOGGER.info("Filtering out Tag supervison notification as newer value in cache - tag id is " + tag.getId());
    }
  }
  
  /**
   * Private method for notifying all listeners registered for supervision changes 
   * to Tags (i.e. they get a Tag update call on a supervision change also)
   * 
   * @param tag Tag copy (so not in cache), should be invalidated with supervision changes if necessary
   */
  private void notifyListenersWithSupervision(final T tag) {
    listenerLock.readLock().lock();
    try {           
      for (CacheSupervisionListener< ? super T> cacheListener : listenersWithSupervision) {        
        cacheListener.onSupervisionChange(tag);
      }                 
    } finally {
      listenerLock.readLock().unlock();
    }
  }
  
  @Override
  public void registerListenerWithSupervision(CacheSupervisionListener< ? super T> timCacheListener) {
    listenerLock.writeLock().lock();
    try {
      listenersWithSupervision.add(timCacheListener);
    } finally {
      listenerLock.writeLock().unlock();
    }    
  }

}
