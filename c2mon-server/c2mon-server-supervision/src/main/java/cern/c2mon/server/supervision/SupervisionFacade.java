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

import cern.c2mon.shared.client.supervision.SupervisionEvent;

import java.util.Collection;

/**
 * Provides methods for querying the Supervision status of
 * components in the system (DAQ, Equipment or SubEquipment).
 * 
 * @author Mark Brightwell
 *
 */
public interface SupervisionFacade {

  /**
   * Returns the supervision status of all components of the
   * system, that is DAQ, Equipment, SubEquipment.
   * 
   * <p>The returned list of SupervisionEvents have timestamp
   * the time at which the status was checked in the cache.
   * 
   * @return a collection of SupervisionEvents
   */
  Collection<SupervisionEvent> getAllSupervisionStates();

  /**
   * Calls all cache listeners (status confirmation) to Process, Equipment and
   * SubEquipment caches with the current status of these supervised objects. 
   */
  void refreshAllSupervisionStatus();
  
}
