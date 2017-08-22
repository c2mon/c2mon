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
package cern.c2mon.cache.api.service;

import java.util.Properties;

import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

/**
 * Implemented by Cache object facades for caches that
 * allow reconfiguration during runtime.
 * 
 * @author Mark Brightwell
 *
 */
public interface ConfigurableCacheService<T extends Cacheable> {

  /**
   * Creates a cache object with the provided id from
   * the provided Properties object. Should also do some
   * validation of the cache object before it is inserted
   * into the system.
   * 
   * <p>Note this method does NOT insert the cache object into
   * the cache or the database. This should be done in
   * the configuration module.
   * 
   <p>For tag objects, the cache timestamp is set to the time the object was created;
   * the DAQ and source timestamps are set as null (for Data/ControlTags). The quality is
   * set to UNINITIALISED.
   * 
   * @param id the id of the cache object created (should not exist already)
   * @param properties the map of properties necessary for creating
   *            the object
   * @return the configured cache object
   * @throws IllegalAccessException 
   */
  T createCacheObject(Long id, Properties properties) throws IllegalAccessException;

  /**
   * Throws a {@link ConfigurationException} if update not permitted, in 
   * which case the reconfiguration should be aborted (this method should leave
   * the cache in a consistent state in this case).
   * @param cacheable
   * @param properties
   * @return a Change event containing the changes needed to send to the DAQ
   *          (may contain no changes)
   * @throws IllegalAccessException 
   */
  Change updateConfig(T cacheable, Properties properties) throws IllegalAccessException;
}
