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

import java.util.List;

import cern.c2mon.server.cache.dbaccess.structure.DBBatch;
import cern.c2mon.shared.common.Cacheable;

/**
 * Required for caches that use the batch loading mechanism.
 *
 * @author Mark Brightwell
 *
 */
public interface BatchLoaderMapper<T extends Cacheable> extends SimpleLoaderMapper<T> {

  /**
   * Return a list of records from the DB
   *
   * @param dbBatch specifies the batch of cache objects to load
   * @return the list of records
   */
  List<T> getRowBatch(DBBatch dbBatch);

  /**
   * Returns the maximum id of the cache objects to load.
   *
   * @return max id
   */
  Long getMaxId();

  /**
   * Returns the minimum id of the cache objects to load.
   *
   * @return min id
   */
  Long getMinId();
}
