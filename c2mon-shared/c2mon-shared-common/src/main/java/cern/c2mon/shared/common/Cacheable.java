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

import java.io.Serializable;

/**
 * Common interface for all objects that reside in TIM caches.
 *
 * @author Mark Brightwell
 *
 */
public interface Cacheable extends Serializable, Cloneable  {

  /**
   * All objects have a Long id.
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
}
