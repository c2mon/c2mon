/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.cachepersistence;

import java.util.List;

import cern.c2mon.shared.common.Cacheable;

/**
 * Interface of cache DAOs used to persist cache updates
 * to the database.
 * 
 * @author Mark Brightwell
 * 
 * @param <T> the type in the cache
 */
public interface CachePersistenceDAO<T extends Cacheable> {
  
  /**
   * Persist a single updated cache object to the DB.
   * (only value fields are saved! i.e. those that change
   * due to incoming updates).
   * 
   * @param cacheable the cache object to persist
   */
  void updateCacheable(T cacheable);

  /**
   * Persists a batch of cache objects in a single transaction.
   * 
   * @param keyList keys of the cache objects to persist
   */
  void persistBatch(List<Long> keyList);

}
