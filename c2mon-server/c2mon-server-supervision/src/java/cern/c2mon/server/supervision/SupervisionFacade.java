/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.supervision;

import java.util.Collection;

import cern.c2mon.shared.client.supervision.SupervisionEvent;

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
 
  /**
   * Refreshes the state tags with new timestamp and the latest value.
   * Used when trying to make cache consistent after crash.
   * 
   * <p>Do not call while holding locks.
   */
  void refreshStateTags();
  
}
