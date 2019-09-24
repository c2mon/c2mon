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
package cern.c2mon.cache.api.listener;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.Cacheable;

/**
 * Implemented by classes wishing to receive
 * supervision invalidation/validation callbacks
 * on <b>all</b> tags on supervision status changes
 * of DAQs or Equipment.
 *
 * <p>Prefer the use of a SupervisionListener when
 * a single notification for the DAQ/Equipment is
 * feasible.
 *
 * <p>Callbacks are only made when a Process/Equipment
 * moves from running to down or vice-versa.
 *
 * @author Mark Brightwell
 * @param <T> the type in the cache
 *
 */
public interface CacheSupervisionListener<T extends Cacheable> {

  /**
   * Called when the status of the DAQ/Equipment changes from
   * RUNNING to DOWN/STOPPED and vice-versa.
   *
   * <p>Is called within a lock on the Tag (in the cache, not
   * the passed parameter).
   *
   * @param tag a copy of the Tag with new status applied
   */
  void onSupervisionChange(T tag);

}
