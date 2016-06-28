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
package cern.c2mon.server.cache;

import cern.c2mon.server.common.alive.AliveTimer;

/**
 * The module public interface that should be used to access objects in the
 * AliveTimer cache.
 * 
 * The AliveTimer cache contains the configuration information for all the AliveTimers (process,
 * equipment, subequipment). This cache is loaded at startup and only changes if a DAQ configuration
 * is changed. The current value of the alive timer is kept in the ControlTagCache.
 * 
 * @author Mark Brightwell
 *
 */
public interface AliveTimerCache extends C2monCacheWithListeners<Long, AliveTimer> {

  String cacheInitializedKey = "c2mon.cache.alive.initialized";
  
  /**
   * Get a reference to the AliveTimer in the cache. 
   * 
   * <p>Throws an {@link IllegalArgumentException} if called with a null key and
   * a {@link CacheElementNotFoundException} if the object was not found in the 
   * cache (both unchecked).
   * 
   * <p>If unsure whether an element is in the cache, first use the <java>hasKey(Long)</java> 
   * method to determine if it is present.
   * 
   * 
   * @param id the id (key) of the AliveTimer cache element
   * @return a reference to the object stored in the cache
   */
  //AliveTimer get(Long id);
  
  /**
   * Notifies the cache listeners that an update has been performed for
   * the control tag with this id.
   * 
   * This method should be called within a block synchronized on the ControlTag object,
   * since it may otherwise be modified by another thread before the listeners
   * are notified (and get their copy of the new value).
   * 
   * NOT YET USED
   * 
   * @param aliveTimer the cache object modified
   */
  //void notifyListenersOfUpdate(AliveTimer aliveTimer);
}
