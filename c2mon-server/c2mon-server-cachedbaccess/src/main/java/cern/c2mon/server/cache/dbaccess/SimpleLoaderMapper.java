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
package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.shared.common.Cacheable;

/**
 * Basic cache loader interface needed for all caches. Provides a method for
 * fetching a single cache element in the database.
 *
 * @author Mark Brightwell
 * @param <T> type of cache object
 *
 */
public interface SimpleLoaderMapper<T extends Cacheable> {

  /**
   * Returns null if not found in the database.
   *
   * @param id the id of the cache object
   * @return the cache object
   */
  T getItem(Object id);

  /**
   * Checks if the current item can be found in the DB.
   *
   * @param id key of the cache element
   * @return true if the record is found
   */
  boolean isInDb(Long id);

  /**
   * Returns the number of records of this type in the DB.
   *
   * @return the number of records
   */
  int getNumberItems();
}
