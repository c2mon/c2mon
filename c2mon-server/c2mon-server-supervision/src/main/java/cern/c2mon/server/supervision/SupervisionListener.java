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
package cern.c2mon.server.supervision;

import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;

/**
 * Interface that must be implemented by any class interested
 * in receiving supervision updates.
 *
 * <p>The listener must then be registered with the SupervisionNotifier
 * bean.
 *
 * @author Mark Brightwell
 *
 */
public interface SupervisionListener extends CacheListener<Supervised> {

  /**
   * Called when the C2MON server detects a change in the supervision
   * status of one of the supervised entities (Process, Equipment, Sub-equipment).
   * If registered on multiple threads may be called unordered.
   *
   * @param supervisionEvent the event details (all fields except String message
   * can be assumed non null)
   */
  void notifySupervisionEvent(SupervisionEvent supervisionEvent);

  @Override
  default void apply(Supervised cacheable) {
    notifySupervisionEvent(cacheable.getSupervisionEvent());
  }
}
