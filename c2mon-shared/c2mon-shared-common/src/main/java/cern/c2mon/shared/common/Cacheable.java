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

import lombok.NonNull;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Common interface for all objects that reside in C2MON caches.
 *
 * @author Mark Brightwell
 * @implSpec It is expected that all implementation have a proper equals and hashCode implementation, as Cacheables
 * may be put in maps and lists, as well as be compared for equality.
 */
public interface Cacheable extends Serializable, Cloneable {

  /**
   * All objects have a long id.
   *
   * @return the id of the cache object
   */
  long getId();

  /**
   * Needed for cache listeners, which always clone the object before
   * notifying the listener.
   *
   * @return the clone
   */
  Cacheable clone();

  Timestamp getCacheTimestamp();

  void setCacheTimestamp(@NonNull Timestamp timestamp);
}
