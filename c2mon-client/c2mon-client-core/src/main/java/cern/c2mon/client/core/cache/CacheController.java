/*******************************************************************************
 * Copyright (C) 2010-2017 CERN. All rights not expressly granted are reserved.
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
 ******************************************************************************/

package cern.c2mon.client.core.cache;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.core.tag.TagController;

/**
 * The <code>CacheController</code> manages the two cache instances
 * (history and live cache) and provides synchronization locks for the
 * {@link TagCache}.
 *
 * @author Matthias Braeger
 */
@Slf4j
@Service
public class CacheController {

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
  CacheController() {
    activeCache = liveCache;
  }

  /**
   * @return The current active cache reference, which is either the
   *         live cache or the history cache.
   */
  public Map<Long, TagController> getActiveCache() {
    return activeCache;
  }
  
  public Map<Long, TagController> getHistoryCache() {
    return historyCache;
  }
  
  public Map<Long, TagController> getLiveCache() {
    return liveCache;
  }
  
  public boolean isHistoryModeEnabled() {
    return historyMode;
  }

  /**
   * The returning object can be used for preventing any thread changing the
   * the cache mode. The {@link #setHistoryMode(boolean)} method is internally
   * synchronizing on the same object.
   * @return The synchronization object for locking other thread changing the
   *         cache mode.
   * @see #setHistoryMode(boolean)
   */
  public Object getHistoryModeSyncLock() {
    return historyModeLock;
  }

  /**
   * Enables or disables the History mode of the cache. In history mode all
   * getter-methods will then return references to objects in the history cache.
   * Also the registered <code>DataTagUpdateListener</code>'s will then receive
   * updates from the history cache.
   * <p>
   * However, the internal live cache is still update will live events and stays
   * up to date once it is decided to switch back into live mode.
   * <p>
   * Please note that this method can be locked by other threads. Locking is
   * realized with the {@link #getHistoryModeSyncLock()} method.
   * <p>
   * This method shall only be used by the {@link HistoryManager}
   *
   * @param enable <code>true</code>, for enabling the history mode
   * @see #getHistoryModeSyncLock()
   */
  public void setHistoryMode(final boolean enable) {
    synchronized (historyModeLock) {
      if (historyMode == enable) {
        log.info("setHistoryMode() - The cache is already in history mode.");
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
  
  public WriteLock getWriteLock() {
    return cacheLock.writeLock();
  }
  
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
