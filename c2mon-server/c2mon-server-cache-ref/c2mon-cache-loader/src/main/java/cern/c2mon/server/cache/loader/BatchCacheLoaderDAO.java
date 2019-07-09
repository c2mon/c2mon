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

import java.util.Map;

import cern.c2mon.shared.common.Cacheable;

/**
 * DAO that must be provided for using the C2MON batch loading mechanism.
 *
 * @param <V> the type of cache object
 *
 * @author Mark Brightwell
 */
public interface BatchCacheLoaderDAO<V extends Cacheable> extends SimpleCacheLoaderDAO<V> {

  /**
   * Returns the highest row number of elements to be loaded. Returns 0 if no
   * cache objects are defined.
   *
   * @return the highest id
   */
  Integer getMaxRow();

  /**
   * Return a map of elements that need loading. All elements with row number between
   * the provided numbers are returned.
   * <p>
   * <p>This method also performs post-DB-access logic to the object itself, using
   * the implemented doPostDbLoading method.
   *
   * @param firstRow fetches all elements with rows above or including this number
   * @param lastRow  fetches all elements with rows below or including this number
   *
   * @return a map of objects ready to load into the cache
   */
  Map<Long, V> getBatchAsMap(Long firstRow, Long lastRow);

}
