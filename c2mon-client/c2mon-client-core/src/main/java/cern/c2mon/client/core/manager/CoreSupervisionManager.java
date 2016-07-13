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
package cern.c2mon.client.core.manager;

import java.util.Collection;

import cern.c2mon.client.core.C2monSupervisionManager;
import cern.c2mon.client.core.SupervisionService;
import cern.c2mon.client.core.jms.SupervisionListener;

/**
 * This interface extends the <code>C2monSupervisionManager</code>
 * interface and provides additional functionalities to interact with
 * the <code>SupervisionManager</code>. This interface is only
 * visible for other manager classes in that package.
 *
 * @author Matthias Braeger
 */
public interface CoreSupervisionManager extends C2monSupervisionManager, SupervisionService {

  /**
   * Registers a <code>SupervisionListener</code> which is then getting informed
   * about <code>SupervisionEvent</code> updates for the registered processes and
   * equipments
   *
   * @param listener The listener to be registered
   * @param processIds List of process id's for which the listeners wants to receive notifications
   * @param equipmentIds List of equipment id's for which the listeners wants to receive notifications
   * @param subEquipmentIds List of sub equipment id's for which the listeners wants to receive notifications
   */
  void addSupervisionListener(SupervisionListener listener, final Collection<Long> processIds, final Collection<Long> equipmentIds,
      final Collection<Long> subEquipmentIds);

  /**
   * Removes a previously registered <code>SupervisionListener</code> from all notifications.
   * @param listener The listener that shall be removed
   */
  void removeSupervisionListener(SupervisionListener listener);

  /**
   * Synchronizes all supervision status with the C2MON server
   */
  void refreshSupervisionStatus();
}
