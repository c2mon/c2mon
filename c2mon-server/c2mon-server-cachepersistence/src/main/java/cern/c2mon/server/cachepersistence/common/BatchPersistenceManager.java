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
package cern.c2mon.server.cachepersistence.common;

import java.util.Collection;

/**
 * Bean managing the batch persistence of updates held in the cache
 * to the database. A new instance of this bean should be instantiated
 * for each cache requiring update persistence.
 * 
 * @author Mark Brightwell
 *
 */
public interface BatchPersistenceManager {
  
  /**
   * Persist this list of updates to the cache. A collection
   * of ids are passed, with the intention that the current
   * cache values should be used.
   * 
   * @param cacheableIds a list of cache object ids
   */
  void persistList(Collection<Long> cacheableIds);

  /**
   * Add a single cache object to the list of those that need persisting.
   * The object will be persisted with the next persistence batch, *or at
   * shutdown only, if this cache is not updated again*.
   * @param key key of cache object to persist
   */
  void addElementToPersist(Long key);

}
