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

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.core.tag.TagController;

@Service
class CacheControllerImpl implements CacheController {
  /** Logger instance */
  private static final Logger LOG = LoggerFactory.getLogger(CacheControllerImpl.class);
  
  /**
   * Pointer to the actual used cache instance (live or history)
   */
  private Map<Long, TagController> activeCache = null;
  
  /** Thread synchronization lock for avoiding a cache mode switch */ 
  private final Object historyModeLock = new Object();
  
  /** 
   * <code>Map</code> containing all subscribed data tags which are updated via the
   * <code>JmsProxy</code>
   */
  private final Map<Long, TagController> liveCache = new Hashtable<>(1500);
  
  /** 
   * <code>Map</code> containing all subscribed data tags which are updated via the
   * <code>HistoryManager</code>
   */
  private final Map<Long, TagController> historyCache = new Hashtable<>(1500);
  
  /**
   * Flag to remember whether the cache is in history mode or not
   */
  private volatile boolean historyMode = false;
  
  /** Thread lock for access to the <code>dataTags</code> Map */
  private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
  
  /**
   * Default Constructor
   */
  CacheControllerImpl() {
    activeCache = liveCache;
  }
  
  @Override
  public Map<Long, TagController> getActiveCache() {
    return activeCache;
  }
  
  @Override
  public Map<Long, TagController> getHistoryCache() {
    return historyCache;
  }
  
  @Override
  public Map<Long, TagController> getLiveCache() {
    return liveCache;
  }
  
  @Override
  public boolean isHistoryModeEnabled() {
    return historyMode;
  }
  
  @Override
  public Object getHistoryModeSyncLock() {
    return historyModeLock;
  }
  
  @Override
  public void setHistoryMode(final boolean enable) {
    synchronized (historyModeLock) {
      if (historyMode == enable) {
        LOG.info("setHistoryMode() - The cache is already in history mode.");
        return;
      }
      
      cacheLock.readLock().lock();
      try {
        if (enable) {
          enableHistoryMode();
        }
        else {
          disableHistoryMode();
        }
      }
      finally {
        cacheLock.readLock().unlock();
      }
      
      historyMode = enable;
    }
  }
  
  @Override
  public WriteLock getWriteLock() {
    return cacheLock.writeLock();
  }
  
  @Override
  public ReadLock getReadLock() {
    return cacheLock.readLock();
  }
  
  /**
   * Inner method which moves all registered <code>DataTagUpdateListener</code>
   * listeners back to the live cache instance.
   */
  private void disableHistoryMode() {
    TagController historyTag = null;
    Collection<BaseTagListener> listeners = null;
    
    for (Entry<Long, TagController> entry : historyCache.entrySet()) {
      historyTag = entry.getValue();
      listeners = historyTag.getUpdateListeners();
      
      historyTag.removeAllUpdateListeners();
      liveCache.get(entry.getKey()).addUpdateListeners(listeners);
    }
    
    activeCache = liveCache;
    historyCache.clear();
  }
  
  /**
   * Inner method which clones the entire live cache and moves all registered
   * <code>DataTagUpdateListener</code> listeners to the history cache instance.
   */
  private void enableHistoryMode() {
    historyCache.clear();
    TagController liveTag = null;
    TagController historyTag = null;
    Collection<BaseTagListener> listeners = null;
    
    for (Entry<Long, TagController> entry : liveCache.entrySet()) {
      liveTag = entry.getValue();
      
      historyTag = new TagController(liveTag.getTagImpl().clone());
      
      listeners = liveTag.getUpdateListeners();
      liveTag.removeAllUpdateListeners();
      historyTag.addUpdateListeners(listeners);
      historyCache.put(entry.getKey(), historyTag);
    }
  
    activeCache = historyCache;
  }
}
