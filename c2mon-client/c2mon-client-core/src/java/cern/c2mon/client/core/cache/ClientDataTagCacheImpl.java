/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.client.core.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.core.tag.ClientDataTagImpl;

/**
 * This class implements the cache of the C2MON client API. The public method
 * provided by this class are never accessed directly by the application layer.
 * Only the <code>TagManager</code> has a reference to the cache service and is
 * controlling the access to it.
 * <p>
 * The cache provides a <code>create()</code> method for creating a new
 * <code>ClientDataTag</code> cache entry. In the background it handles also the
 * subscription to the incoming live events. Only the initialization of the tag
 * is performed by the <code>TagManager</code>.
 * <p>
 * It is possible to switch the <code>ClientDataTagCache</code> from live mode
 * into history mode and back. Therefore this class manages internally two
 * <code>ClientDataTag</code> map instances, one for live tag updates and the
 * other for historical events. Depending on the cache mode the getter methods
 * return either references to the live tags or to the history tags. 
 *
 * @author Matthias Braeger
 */
@Service
public class ClientDataTagCacheImpl implements ClientDataTagCache {

  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(ClientDataTagCacheImpl.class);
  
  /** The cache controller manages the cache references */
  private final CacheController controller;
  
  /** The cache Synchronizer*/
  private final CacheSynchronizer cacheSynchronizer;
  
  /** 
   * <code>Map</code> reference containing all subscribed data tags which
   * are updated via the <code>JmsProxy</code>
   */
  private Map<Long, ClientDataTagImpl> liveCache = null;
  
  /** Reference to the cache read lock */
  private ReadLock cacheReadLock = null;
  
  /** Reference to the cache write lock */
  private WriteLock cacheWriteLock = null;
  
  /**
   * Default Constructor used by Spring to wire in the references to other Services.
   * 
   * @param pCacheController Provides acces to the different cache instances and to the thread locks.
   * @param pCacheSynchronizer Handles the cache synchronization with the C2MON server
   * 
   */
  @Autowired
  protected ClientDataTagCacheImpl(final CacheController pCacheController,
                                   final CacheSynchronizer pCacheSynchronizer) {
    this.controller = pCacheController;
    this.cacheSynchronizer = pCacheSynchronizer;
  }
  
  /**
   * This method is called by Spring after having created this service.
   */
  @PostConstruct
  protected void init() {
    cacheReadLock = controller.getReadLock();
    cacheWriteLock = controller.getWriteLock();
    liveCache = controller.getLiveCache();
  }
  
  @Override
  public ClientDataTag get(final Long tagId) {
    ClientDataTag cdt = null;
     
    cacheReadLock.lock();
    try {
      cdt = controller.getActiveCache().get(tagId);
    }
    finally { cacheReadLock.unlock(); }
  
    return cdt;
  }
  
  @Override
  public Collection<ClientDataTag> getAllSubscribedDataTags() {
    Collection<ClientDataTag> list = new ArrayList<ClientDataTag>(controller.getActiveCache().size());
    
    cacheReadLock.lock();
    try {
      for (ClientDataTagImpl cdt : controller.getActiveCache().values()) {
        if (cdt.hasUpdateListeners()) {
          list.add(cdt);
        }
      }
    }
    finally { cacheReadLock.unlock(); }
    
    return list;
  }

  @Override
  public Collection<ClientDataTag> getAllTagsForEquipment(final Long equipmentId) {
    Collection<ClientDataTag> list = new ArrayList<ClientDataTag>();
  
    cacheReadLock.lock();
    try {
      for (ClientDataTag cdt : controller.getActiveCache().values()) {
        if (cdt.getEquipmentIds().contains(equipmentId)) {
          list.add(cdt);
        }
      }
    }
    finally { cacheReadLock.unlock(); }
    
    return list;
  }

  @Override
  public Collection<ClientDataTag> getAllTagsForListener(final DataTagUpdateListener listener) {
    Collection<ClientDataTag> list = new ArrayList<ClientDataTag>();

    cacheReadLock.lock();
    try {
      for (ClientDataTagImpl cdt : controller.getActiveCache().values()) {
        if (cdt.isUpdateListenerRegistered(listener)) {
          list.add(cdt);
        }
      }
    }
    finally { cacheReadLock.unlock(); }
    
    return list;
  }
  
  @Override
  public Set<Long> getAllTagIdsForListener(final DataTagUpdateListener listener) {
    Set<Long> list = new HashSet<Long>();

    cacheReadLock.lock();
    try {
      for (ClientDataTagImpl cdt : controller.getActiveCache().values()) {
        if (cdt.isUpdateListenerRegistered(listener)) {
          list.add(cdt.getId());
        }
      }
    }
    finally { cacheReadLock.unlock(); }
    
    return list;
  }

  @Override
  public Collection<ClientDataTag> getAllTagsForProcess(final Long processId) {
    Collection<ClientDataTag> list = new ArrayList<ClientDataTag>();

    cacheReadLock.lock();
    try {
      for (ClientDataTag cdt : controller.getActiveCache().values()) {
        if (cdt.getProcessIds().contains(processId)) {
          list.add(cdt);
        }
      }
    }
    finally { cacheReadLock.unlock(); }
    
    return list;
  }
  
  
  @Override
  public void refresh() {
    cacheSynchronizer.refresh(null);
  }
  
  @Override
  public void refresh(final Set<Long> tagIds) {
    cacheSynchronizer.refresh(tagIds);
  }


  @Override
  public Set<Long> unsubscribeAllDataTags(final DataTagUpdateListener listener) {
    Set<Long> tagsToRemove = new HashSet<Long>();
    cacheWriteLock.lock();
    try {
      for (ClientDataTagImpl cdt : controller.getActiveCache().values()) {
        if (cdt.isUpdateListenerRegistered(listener)) {
          cdt.removeUpdateListener(listener);
          if (!cdt.hasUpdateListeners()) {
            tagsToRemove.add(cdt.getId());
          }
        }
      }
      
      // Remove from cache
      cacheSynchronizer.removeTags(tagsToRemove);
    }
    finally { cacheWriteLock.unlock(); }
    
    return tagsToRemove;
  }

  @Override
  public Set<Long> unsubscribeDataTags(final Set<Long> dataTagIds, final DataTagUpdateListener listener) {
    Set<Long> tagsToRemove = new HashSet<Long>();
    cacheWriteLock.lock();
    try {
      ClientDataTagImpl cdt = null;
      for (Long tagId : dataTagIds) {
        cdt = controller.getActiveCache().get(tagId);
        if (cdt != null) {
          cdt.removeUpdateListener(listener);
          if (!cdt.hasUpdateListeners()) {
            tagsToRemove.add(tagId);
          }
        }
      }
      
      // Remove from cache
      cacheSynchronizer.removeTags(tagsToRemove);
    }
    finally { cacheWriteLock.unlock(); }
    
    return tagsToRemove;
  }

  @Override
  public Map<Long, ClientDataTag> get(final Set<Long> tagIds) {
    Map<Long, ClientDataTag> resultMap = new HashMap<Long, ClientDataTag>(tagIds.size());
    cacheReadLock.lock();
    try {
      for (Long tagId : tagIds) {
        resultMap.put(tagId, controller.getActiveCache().get(tagId));
      }
    }
    finally { cacheReadLock.unlock(); }
    
    return resultMap;
  }

  @Override
  public boolean isHistoryModeEnabled() {
    return controller.isHistoryModeEnabled();
  }

  @Override
  public void setHistoryMode(final boolean enable) {
    controller.setHistoryMode(enable);
  }
  
  @Override
  public Object getHistoryModeSyncLock() {
    return controller.getHistoryModeSyncLock();
  }

  @Override
  public boolean containsTag(final Long tagId) {
    return controller.getActiveCache().containsKey(tagId);
  }

  @Override
  public Set<Long> addDataTagUpdateListener(final Set<Long> tagIds, final DataTagUpdateListener listener) {
    Set<Long> newTagIds = new HashSet<Long>();
    synchronized (getHistoryModeSyncLock()) {
      cacheWriteLock.lock();
      try {
        ClientDataTagImpl cdt = null;
        for (Long tagId : tagIds) {
          if (liveCache.containsKey(tagId)) {
            cdt = controller.getActiveCache().get(tagId);
            cdt.addUpdateListener(listener);
          }
          else {
            newTagIds.add(tagId);
          }
        }
        
        cacheSynchronizer.createTags(newTagIds);
        for (Long tagId : newTagIds) {
          cdt = controller.getActiveCache().get(tagId);
          cdt.addUpdateListener(listener);
        }
      }
      finally { cacheWriteLock.unlock(); }
    } // end of synchronization
    
    return newTagIds;
  }

  @Override
    public int getCacheSize() {
      
      return liveCache.size();
    }
}
