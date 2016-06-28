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

import cern.c2mon.shared.common.Cacheable;

/**
 * Needs implementing by cache loaders that load all objects
 * on a single thread (for caches with few objects).
 * 
 * @author Mark Brightwell
 * 
 * @param <T> the type in the cache
 *
 */
public interface LoaderMapper<T extends Cacheable> extends SimpleLoaderMapper<T> {
  
  /**
   * Method responsible for fetching the DataTagCacheObjects from the DB
   * (DATATAG table in TIMPRO).
   * @return a list of cache objects
   */
  List<T> getAll();
  
}
