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
package cern.c2mon.server.cache.loader;

import cern.c2mon.shared.common.Cacheable;

/**
 * Interface used during reconfiguration of the cache.
 *
 * @param <T> the cache object type
 *
 * @author Mark Brightwell
 */
public interface ConfigurableDAO<T extends Cacheable> {

  /**
   * Delete this cache object from the cache DB.
   *
   * @param id the cache object unique id
   */
  void deleteItem(Long id);

  /**
   * Saves the configuration fields of the cache object to the database.
   * Is used when a DataTag or other object has been reconfigured (UPDATE
   * reconfiguration).
   *
   * @param cacheable the tag object where the configuraton fields have
   *                  been updated
   */
  void updateConfig(T cacheable);

  /**
   * Inserts this cache object into the DB.
   *
   * @param cacheable the object to insert
   */
  void insert(T cacheable);

}
