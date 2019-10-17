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
package cern.c2mon.shared.common;

import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;

/**
 * Common interface for all objects that reside in C2MON caches.
 *
 * @author Mark Brightwell
 * @implSpec It is expected that all implementation have a proper equals and hashCode implementation, as Cacheables
 * may be put in maps and lists, as well as be compared for equality.
 */
public interface Cacheable extends Serializable, Cloneable {

  /**
   * All objects have a Long id.
   *
   * @return the id of the cache object
   */
  Long getId();

  /**
   * Needed for cache listeners, which always clone the object before
   * notifying the listener.
   *
   * @return the clone
   * @throws CloneNotSupportedException if not implemented/supported so far
   */
  Cacheable clone() throws CloneNotSupportedException;

  Timestamp getCacheTimestamp();

  void setCacheTimestamp(Timestamp timestamp);

  /**
   * Validates that {@code this} object should be inserted.
   * <p>
   * Typical uses of this method would be to verify that the object is complete and
   * correct, while also able to test against the previous object, e.g for a later
   * timestamp. Be wary that the previous object will be {@code null} during the
   * first value insertion.
   * <p>
   * Parallelism / Concurrency:
   * <ul>
   *   <li> This method can have side effects. If {@code this} mutates the changes
   *        will stay with it as it is put in the cache.
   *   <li> While {@code previous} is a frozen clone, be wary of {@code this}
   *        being modified from another thread.
   * </ul>
   *
   * @param previous potentially null, the previous object if one existed
   * @param <T>      type of previous object, should match the type of {@code this}
   * @return boolean true if this object should be put in the cache - false otherwise
   */
  <T extends Cacheable> boolean preInsertValidate(@Nullable T previous);

  /**
   * Executes any post insertion logic and creates all post insertion events
   * <p>
   * Any mutations to this object will not affect the cache in any way. The object
   * has already been inserted to the cache and changing this reference will do
   * nothing to change that reference, until you explicitly do {@code Cache.put}
   *
   * @param previous potentially null, the previous object if one existed
   * @param <T>      type of previous object, should match the type of {@code this}
   * @return a set of all {@link CacheEvent}s that should be fired based on this change
   */
  <T extends Cacheable> Set<CacheEvent> postInsertEvents(@Nullable T previous);
}
