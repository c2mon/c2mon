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
package cern.c2mon.server.cache.loading.common;


/**
 * Interface that must be implemented by all C2MON
 * cache loading mechanisms.
 *
 * @author Mark Brightwell
 *
 */
public interface C2monCacheLoader {

  /**
   * Lock used for synchronising all servers at start up. This takes
   * place during the alive start up: read locks are used for each of
   * the cache loading mechanisms and a write lock is acquired at the
   * alive start up.
   *
   * An associated Boolean flag in the ClusterCache is indicating if
   * the alive mechanisms was started for all the DAQs loaded into
   * the cache (performed once by a singleserver at start up).
   */
  String aliveStatusInitialized = "c2mon.cache.aliveStatusInitialized";

  /**
   * At server start-up, loads the cache from the DB into memory.
   * In distributed set-up, this is not performed once the TC
   * cache has already been loaded once (only performed if the disk
   * store is cleaned).
   */
  void preload();

}
