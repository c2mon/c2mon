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

import cern.c2mon.client.common.listener.BaseListener;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.core.tag.CloneableTagBean;

@Service
class CacheControllerImpl implements CacheController {
  /** Logger instance */
  private static final Logger LOG = LoggerFactory.getLogger(CacheControllerImpl.class);
  
  /**
   * Pointer to the actual used cache instance (live or history)
   */
  private Map<Long, CloneableTagBean> activeCache = null;
  
  /** Thread synchronization lock for avoiding a cache mode switch */ 
  private final Object historyModeLock = new Object();
  
  /** 
   * <code>Map</code> containing all subscribed data tags which are updated via the
   * <code>JmsProxy</code>
   */
  private final Map<Long, CloneableTagBean> liveCache = new Hashtable<>(1500);
  
  /** 
   * <code>Map</code> containing all subscribed data tags which are updated via the
   * <code>HistoryManager</code>
   */
  private final Map<Long, CloneableTagBean> historyCache = new Hashtable<>(1500);
  
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
  public Map<Long, CloneableTagBean> getActiveCache() {
    return activeCache;
  }
  
  @Override
  public Map<Long, CloneableTagBean> getHistoryCache() {
    return historyCache;
  }
  
  @Override
  public Map<Long, CloneableTagBean> getLiveCache() {
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
    CloneableTagBean historyTag = null;
    Collection<BaseListener> listeners = null;
    
    for (Entry<Long, CloneableTagBean> entry : historyCache.entrySet()) {
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
    CloneableTagBean liveTag = null;
    CloneableTagBean historyTag = null;
    Collection<BaseListener> listeners = null;
    
    for (Entry<Long, CloneableTagBean> entry : liveCache.entrySet()) {
      liveTag = entry.getValue();
      
      historyTag = new CloneableTagBean(liveTag.getTagBean().clone());
      
      listeners = liveTag.getUpdateListeners();
      liveTag.removeAllUpdateListeners();
      historyTag.addUpdateListeners(listeners);
      historyCache.put(entry.getKey(), historyTag);
    }
  
    activeCache = historyCache;
  }
}
