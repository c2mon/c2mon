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
package cern.c2mon.server.configuration.handler;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

import java.util.List;
import java.util.Properties;

/**
 * Bean managing configuration updates to C2MON Equipment.
 *
 * @author Mark Brightwell
 */
public interface EquipmentConfigHandler {

  /**
   * Creates an Equipment in the C2MON server.
   *
   * @param element contains details of the Equipment
   * @return ProcessChange with id of Process that requires restart (not currently
   * supported by DAQ layer)
   * @throws IllegalAccessException
   */
  List<ProcessChange> create(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Updates an Equipment in the C2MON server.
   *
   * @param equipmentId       the id of the Equipment to update
   * @param elementProperties details of the fields to modify
   * @return a list of changes to send to the DAQ layer
   */
  List<ProcessChange> update(Long equipmentId, Properties elementProperties) throws IllegalAccessException;

  /**
   * Removes an Equipment from the C2MON server.
   *
   * @param equipmentId     the id of the Equipment to remove
   * @param equipmentReport the report for this event;
   *                        is passed as parameter so cascaded actions can attach subreports
   * @return always returns a change object requiring restart (remove not supported on DAQ layer so far)
   */
  ProcessChange remove(Long equipmentId, ConfigurationElementReport equipmentReport);

  void setProcessConfigHandler(ProcessConfigHandler processConfigHandler);

}
