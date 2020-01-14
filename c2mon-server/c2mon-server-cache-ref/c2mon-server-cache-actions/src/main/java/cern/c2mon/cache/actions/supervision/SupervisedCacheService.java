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
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.flow.CacheUpdateFlow;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import lombok.NonNull;

import java.sql.Timestamp;
import java.util.List;

/**
 * Implemented by beans linked to Supervised cache
 * objects (so Process, Equipment and SubEquipment).
 *
 * @param <T> the cache object type
 * @author Mark Brightwell
 */
public interface SupervisedCacheService<T extends Supervised> extends AbstractCacheService<T> {

  /**
   * Returns the last supervision event that occured
   * for this supervised cache object (or a generated
   * one if none have yet occured for this cache object).
   *
   * @param id id of the supervised cache object
   * @return the last supervision event
   */
  SupervisionEvent getSupervisionEvent(long id);

  /**
   * Notifies all registered listeners of the current supervision status
   * of the cache element with given id. The timestamp of all events is refreshed
   * to the current time. Is used for example on server startup
   * to "refresh" all listeners in cache of server failure.
   *
   * @param id id of the cache element
   */
  void refresh(long id);

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
  T start(long id, @NonNull Timestamp timestamp);

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

  /**
   * Returns true if the object in the cache is either running or in the
   * start up phase. And false if either DOWN or STOPPED, or
   * is UNCERTAIN.
   *
   * @param id of the cache object
   * @return true if running
   */
  boolean isRunning(long id);

  /**
   * Returns true only if the object in the cache is in UNCERTAIN status.
   *
   * @param id of the cache object
   * @return true if the status is uncertain
   */
  boolean isUncertain(long id);

  /**
   * Stops and removes the alive timer object from the cache.
   *
   * @param id of supervised object
   * @throws CacheElementNotFoundException if the supervised object cannot be located in the corresponding cache
   */
  void removeAliveTimerBySupervisedId(long id);

  void startAliveTimerBySupervisedId(long id);

  SupervisionEntity getSupervisionEntity();

  List<SupervisionEvent> getAllSupervisionEvents();
}
