/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.client.core.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.BaseListener;
import cern.c2mon.client.common.listener.DataTagListener;
import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.listener.TagSubscriptionListener;
import cern.c2mon.client.core.tag.ClientDataTagImpl;

/**
 * This class implements the cache of the C2MON client API. The public method
 * provided by this class are never accessed directly by the application layer.
 * Only the <code>TagServiceImpl</code> has a reference to the cache service and is
 * controlling the access to it.
 * <p>
 * The cache provides a <code>create()</code> method for creating a new
 * <code>Tag</code> cache entry. In the background it handles also the
 * subscription to the incoming live events. Only the initialization of the tag
 * is performed by the <code>TagServiceImpl</code>.
 * <p>
 * It is possible to switch the <code>ClientDataTagCache</code> from live mode
 * into history mode and back. Therefore this class manages internally two
 * <code>Tag</code> map instances, one for live tag updates and the
 * other for historical events. Depending on the cache mode the getter methods
 * return either references to the live tags or to the history tags.
 *
 * @author Matthias Braeger
 */
@Service
public class ClientDataTagCacheImpl implements ClientDataTagCache {

  /** Used to subscribe/unsubscribe listeners from tags */
  private final TagSubscriptionHandler tagSubscriptionHandler;

  /** The cache controller manages the cache references */
  private final CacheController controller;

  /** The cache Synchronizer */
  private final CacheSynchronizer cacheSynchronizer;

  /** Reference to the cache read lock */
  private ReadLock cacheReadLock = null;


  /**
   * Default Constructor used by Spring to wire in the references to other
   * Services.
   *
   * @param tagSubscriptionHandler Used to subscribe/unsubscribe listeners from tags
   * @param cacheController Provides access to the different cache instances
   *          and to the thread locks.
   * @param cacheSynchronizer Handles the cache synchronization with the C2MON
   *          server
   */
  @Autowired
  protected ClientDataTagCacheImpl(final CacheController cacheController,
                                   final CacheSynchronizer cacheSynchronizer,
                                   final TagSubscriptionHandler tagSubscriptionHandler) {
    this.controller = cacheController;
    this.cacheSynchronizer = cacheSynchronizer;
    this.tagSubscriptionHandler = tagSubscriptionHandler;
  }

  /**
   * This method is called by Spring after having created this service.
   */
  @PostConstruct
  protected void init() {
    cacheReadLock = controller.getReadLock();
  }

  @Override
  public Tag get(final Long tagId) {
    Tag cdt = null;

    cacheReadLock.lock();
    try {
      cdt = controller.getActiveCache().get(tagId);
    } finally {
      cacheReadLock.unlock();
    }

    return cdt;
  }
  
  @Override
  public Tag getByName(final String tagName) {
    cacheReadLock.lock();
    try {
      Collection<ClientDataTagImpl> values = controller.getActiveCache().values();
      for (Tag cdt : values) {
        if (cdt.getName().equalsIgnoreCase(tagName)) {
          return cdt;
        }
      }
    } finally {
      cacheReadLock.unlock();
    }

    return null;
  }

  @Override
  public Collection<Tag> getAllSubscribedDataTags() {
    Collection<Tag> list = new ArrayList<Tag>(controller.getActiveCache().size());

    cacheReadLock.lock();
    try {
      for (ClientDataTagImpl cdt : controller.getActiveCache().values()) {
        if (cdt.hasUpdateListeners()) {
          list.add(cdt);
        }
      }
    } finally {
      cacheReadLock.unlock();
    }

    return list;
  }

  @Override
  public Collection<Tag> getAllTagsForEquipment(final Long equipmentId) {
    Collection<Tag> list = new ArrayList<Tag>();

    cacheReadLock.lock();
    try {
      for (Tag cdt : controller.getActiveCache().values()) {
        if (cdt.getEquipmentIds().contains(equipmentId)) {
          list.add(cdt);
        }
      }
    } finally {
      cacheReadLock.unlock();
    }

    return list;
  }

  @Override
  public Collection<Tag> getAllTagsForListener(final BaseListener listener) {
    Collection<Tag> list = new ArrayList<Tag>();

    cacheReadLock.lock();
    try {
      for (ClientDataTagImpl cdt : controller.getActiveCache().values()) {
        if (cdt.isUpdateListenerRegistered(listener)) {
          list.add(cdt);
        }
      }
    } finally {
      cacheReadLock.unlock();
    }

    return list;
  }

  @Override
  public Set<Long> getAllTagIdsForListener(final BaseListener listener) {
    Set<Long> list = new HashSet<Long>();

    cacheReadLock.lock();
    try {
      for (ClientDataTagImpl cdt : controller.getActiveCache().values()) {
        if (cdt.isUpdateListenerRegistered(listener)) {
          list.add(cdt.getId());
        }
      }
    } finally {
      cacheReadLock.unlock();
    }

    return list;
  }

  @Override
  public Collection<Tag> getAllTagsForProcess(final Long processId) {
    Collection<Tag> list = new ArrayList<Tag>();

    cacheReadLock.lock();
    try {
      for (Tag cdt : controller.getActiveCache().values()) {
        if (cdt.getProcessIds().contains(processId)) {
          list.add(cdt);
        }
      }
    } finally {
      cacheReadLock.unlock();
    }

    return list;
  }

  @Override
  public void refresh() throws CacheSynchronizationException {
    cacheSynchronizer.refresh(null);
  }

  @Override
  public void refresh(final Set<Long> tagIds) throws CacheSynchronizationException {
    cacheSynchronizer.refresh(tagIds);
  }

  @Override
  public void unsubscribeAllDataTags(final BaseListener listener) {
    tagSubscriptionHandler.unsubscribeAllTags(listener);
  }

  @Override
  public void unsubscribeDataTags(final Set<Long> dataTagIds, final BaseListener listener) {
    tagSubscriptionHandler.unsubscribeTags(dataTagIds, listener);
  }

  @Override
  public Map<Long, Tag> get(final Set<Long> tagIds) {
    Map<Long, Tag> resultMap = new HashMap<Long, Tag>(tagIds.size());
    cacheReadLock.lock();
    try {
      for (Long tagId : tagIds) {
        resultMap.put(tagId, controller.getActiveCache().get(tagId));
      }
    } finally {
      cacheReadLock.unlock();
    }

    return resultMap;
  }
  
  @Override
  public Map<String, Tag> getByNames(final Set<String> tagNames) {
    Map<String, Tag> resultMap = new HashMap<>(tagNames.size());

    // Initialize result map
    for (String tagName : tagNames) {
      resultMap.put(tagName, null);
    }
    
    cacheReadLock.lock();
    try {
      Collection<ClientDataTagImpl> values = controller.getActiveCache().values();
      for (Tag cdt : values) {
        for (String tagName : tagNames) {
          if (cdt.getName().equalsIgnoreCase(tagName)) {
            resultMap.put(tagName, cdt);
          }
        }
      }
    } finally {
      cacheReadLock.unlock();
    }

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
  public <T extends BaseListener> void subscribe(final Set<Long> tagIds, final T listener) throws CacheSynchronizationException {
    tagSubscriptionHandler.subscribe(tagIds, listener, (listener instanceof DataTagListener) || (listener instanceof TagListener));
  }
  

  @Override
  public int getCacheSize() {
    return controller.getLiveCache().size();
  }
  
  @Override
  public void addTagSubscriptionListener(final TagSubscriptionListener listener) {
    tagSubscriptionHandler.addSubscriptionListener(listener);
  }

  @Override
  public void removeTagSubscriptionListener(final TagSubscriptionListener listener) {
    tagSubscriptionHandler.removeSubscriptionListener(listener);
  }

  @Override
  public <T extends BaseListener> void subscribeByRegex(Set<String> regexList, T listener) throws CacheSynchronizationException {
    tagSubscriptionHandler.subscribeByRegex(regexList, listener, (listener instanceof TagListener));
  }
}
