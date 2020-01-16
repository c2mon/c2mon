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
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import lombok.NonNull;

import java.sql.Timestamp;

/**
 * Implemented by beans linked to Supervised cache
 * objects (so Process, Equipment and SubEquipment).
 *
 * @param <T> the cache object type
 * @author Mark Brightwell
 */
public interface SupervisedCacheService<T extends Supervised> extends AbstractCacheService<T> {

  /**
   * Sets the status of the Supervised object to STARTUP,
   * with associated message, then returns the updated object
   * <p>
   * <p>Starts the alive timer if not already running.
   *
   * @param id        The cache id of the supervised object
   * @param timestamp time of the start
   * @return the cache object, after {@link CacheUpdateFlow#postInsertEvents(Cacheable, Cacheable)}
   */
  void start(long id, @NonNull Timestamp timestamp);

  /**
   * Sets the status of the Supervised object to DOWN,
   * with stop message, then returns the updated object
   * <p>
   * <p>Stops the alive timer for this object is running.
   *
   * @param id        The cache id of the supervised object
   * @param timestamp time of the stop
   * @return the cache object, after {@link CacheUpdateFlow#postInsertEvents(Cacheable, Cacheable)}
   */
  T stop(long id, @NonNull Timestamp timestamp);

  /**
   * Sets the status to running, then returns the updated object.
   * Used when an alive comes back in after an alive
   * expiration, or when a first alive arrives at Process startup.
   *
   * @param id        The cache id of the supervised object
   * @param timestamp time of the running event
   * @param message   details of the event
   * @return the cache object, after {@link CacheUpdateFlow#postInsertEvents(Cacheable, Cacheable)}
   */
  T resume(long id, @NonNull Timestamp timestamp, @NonNull String message);

  /**
   * Called when an alive expires or a commfault tag is received.
   *
   * Returns the updated object.
   *
   * @param id        The cache id of the supervised object
   * @param timestamp time of problem
   * @param message   details
   * @return the cache object, after {@link CacheUpdateFlow#postInsertEvents(Cacheable, Cacheable)}
   */
  T suspend(long id, @NonNull Timestamp timestamp, @NonNull String message);

  SupervisionEntity getSupervisionEntity();
}
