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
package cern.c2mon.cache.api.listener;

import java.util.Collection;

/**
 * C2monCacheListener version that expects collections rather than
 * single cache objects. Can specify the type expected.
 * 
 * @author Mark Brightwell
 * @param <S> the type of collection the listener is expecting
 *
 */
public interface C2monBufferedCacheListener<S> {

  /**
   * Callback when a cache object is modified. Only the key is passed (the listener
   * may access the cache to get the latest values for instance). 
   * 
   * @param collection keys of the objects that have been updated
   * 
   */
  void notifyElementUpdated(Collection<S> collection);
  
  /**
   * Callback used for confirming the value of the caches object. This is
   * used in particular during a system recovery after a crash. Guaranteed
   * actions should be performed, however this call will often duplicate
   * a previous notifyElementUpdated call.
   * 
   * @param eventCollection keys of the cache objects
   */
  void confirmStatus(Collection<S> eventCollection);

  /**
   * Callback used for providing easy access to getting and setting thread name
   */
  String getThreadName();
}
