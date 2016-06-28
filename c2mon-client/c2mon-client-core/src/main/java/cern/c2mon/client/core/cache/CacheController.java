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

import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import cern.c2mon.client.core.tag.ClientDataTagImpl;

/**
 * This interface describes the methods which are provided by the internal
 * cache. The <code>CacheController</code> manages the two cache instances
 * (history and live cache) and provides synchronization locks for the
 * {@link ClientDataTagCache}.
 *
 * @author Matthias Braeger
 */
interface CacheController {
  
  /**
   * @return The current active cache reference, which is either the 
   *         live cache or the history cache.
   */
  Map<Long, ClientDataTagImpl> getActiveCache();
  
  /**
   * @return The live cache reference
   */
  Map<Long, ClientDataTagImpl> getLiveCache();
  
  /**
   * @return The history cache reference
   */
  Map<Long, ClientDataTagImpl> getHistoryCache();
  
  /**
   * @return <code>true</code>, if the history mode of the cache is enabled 
   */
  boolean isHistoryModeEnabled();
  
  /** 
   * The returning object can be used for preventing any thread changing the 
   * the cache mode. The {@link #setHistoryMode(boolean)} method is internally
   * synchronizing on the same object.
   * @return The synchronization object for locking other thread changing the
   *         cache mode.
   * @see #setHistoryMode(boolean)
   */
  Object getHistoryModeSyncLock();
  
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
  void setHistoryMode(boolean enable);

  /**
   * @return A write synchronization lock to the cache
   */
  WriteLock getWriteLock();

  /**
   * @return A read synchronization lock to the cache
   */
  ReadLock getReadLock();
}
