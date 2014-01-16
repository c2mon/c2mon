/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
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
