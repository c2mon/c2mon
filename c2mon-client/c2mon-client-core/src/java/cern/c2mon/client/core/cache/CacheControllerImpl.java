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

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.core.tag.ClientDataTagImpl;

@Service
class CacheControllerImpl implements CacheController {
  /** Logger instance */
  private static final Logger LOG = Logger.getLogger(CacheControllerImpl.class);
  
  /**
   * Pointer to the actual used cache instance (live or history)
   */
  private Map<Long, ClientDataTagImpl> activeCache = null;
  
  /** Thread synchronization lock for avoiding a cache mode switch */ 
  private final Object historyModeLock = new Object();
  
  /** 
   * <code>Map</code> containing all subscribed data tags which are updated via the
   * <code>JmsProxy</code>
   */
  private final Map<Long, ClientDataTagImpl> liveCache = new Hashtable<Long, ClientDataTagImpl>(1500);
  
  /** 
   * <code>Map</code> containing all subscribed data tags which are updated via the
   * <code>HistoryManager</code>
   */
  private final Map<Long, ClientDataTagImpl> historyCache =  new Hashtable<Long, ClientDataTagImpl>(1500);
  
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
  public Map<Long, ClientDataTagImpl> getActiveCache() {
    return activeCache;
  }
  
  @Override
  public Map<Long, ClientDataTagImpl> getHistoryCache() {
    return historyCache;
  }
  
  @Override
  public Map<Long, ClientDataTagImpl> getLiveCache() {
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
      
      cacheLock.writeLock().lock();
      try {
        if (enable) {
          enableHistoryMode();
        }
        else {
          disableHistoryMode();
        }
      }
      finally {
        cacheLock.writeLock().unlock();
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
    ClientDataTagImpl historyTag = null;
    Collection<DataTagUpdateListener> listeners = null;
    
    for (Entry<Long, ClientDataTagImpl> entry : historyCache.entrySet()) {
      historyTag = entry.getValue();
      listeners = historyTag.getUpdateListeners();
      
      historyTag.removeAllUpdateListeners();
      liveCache.get(entry.getKey()).addUpdateListeners(listeners);
    }
    
    activeCache = liveCache;
  }
  
  /**
   * Inner method which clones the entire live cache and moves all registered
   * <code>DataTagUpdateListener</code> listeners to the history cache instance.
   */
  private void enableHistoryMode() {
    historyCache.clear();
    ClientDataTagImpl liveTag = null;
    ClientDataTagImpl historyTag = null;
    Collection<DataTagUpdateListener> listeners = null;
    
    try {
      for (Entry<Long, ClientDataTagImpl> entry : liveCache.entrySet()) {
        liveTag = entry.getValue();
        
        historyTag = liveTag.clone();
        
        listeners = liveTag.getUpdateListeners();
        liveTag.removeAllUpdateListeners();
        historyTag.addUpdateListeners(listeners);
        historyCache.put(entry.getKey(), historyTag);
      }
    }
    catch (CloneNotSupportedException e) {
      LOG.error("enableHistoryMode() - ClientDataTag is not clonable. Please check the code!", e);
      throw new RuntimeException(e);
    }
    activeCache = historyCache;
  }
}
