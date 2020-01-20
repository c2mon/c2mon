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
package cern.c2mon.cache.actions.supervision;

import cern.c2mon.cache.actions.AbstractCacheService;
import cern.c2mon.cache.api.flow.CacheUpdateFlow;
import cern.c2mon.shared.common.Cacheable;
import lombok.NonNull;

import java.sql.Timestamp;

/**
 * Implemented by beans linked to Supervised cache
 * objects (so Process, Equipment and SubEquipment).
 *
 * Also implemented by the ControlTag services (so
 * AliveTag, CommFaultTag and SupervisionStateTag).
 *
 * You can call the ControlTag services directly, but
 * prefer to call the Supervised object ones, and let
 * them cascade the event through the others
 *
 * @param <T> the cache object type
 * @author Mark Brightwell
 */
public interface SupervisedCacheService<T extends Cacheable> extends AbstractCacheService<T> {

  /**
   * Sets the status of the Supervised object to STARTUP,
   * with associated message
   * <p>
   * <p>Starts the alive timer if not already running.
   *
   * For Processes, use {@link cern.c2mon.cache.actions.process.ProcessOperationService#start(Long, String, Timestamp)}
   *
   * @param id        The cache id of the supervised object
   * @param timestamp time of the start
   * @return the cache object, after {@link CacheUpdateFlow#postInsertEvents(Cacheable, Cacheable)}
   */
  void start(long id, long timestamp);

  /**
   * Sets the status of the Supervised object to DOWN,
   * with stop message
   * <p>
   * <p>Stops the alive timer for this object if running.
   *
   * @param id        The cache id of the supervised object
   * @param timestamp time of the stop
   * @return the cache object, after {@link CacheUpdateFlow#postInsertEvents(Cacheable, Cacheable)}
   */
  void stop(long id, long timestamp);

  /**
   * Sets the status to running
   * Used when an alive comes back in after an alive
   * expiration, or when a first alive arrives at Process startup.
   *
   * @param id        The cache id of the supervised object
   * @param timestamp time of the running event
   * @param message   details of the event
   * @return the cache object, after {@link CacheUpdateFlow#postInsertEvents(Cacheable, Cacheable)}
   */
  void resume(long id,long timestamp, @NonNull String message);

  /**
   * Called when an alive expires or a commfault tag is received.
   *
   * @param id        The cache id of the supervised object
   * @param timestamp time of problem
   * @param message   details
   * @return the cache object, after {@link CacheUpdateFlow#postInsertEvents(Cacheable, Cacheable)}
   */
  void suspend(long id, long timestamp, @NonNull String message);
}
