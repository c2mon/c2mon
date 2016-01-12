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

import java.util.List;
import java.util.Properties;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * Bean managing configuration updates to C2MON SubEquipment.
 *
 * @author Mark Brightwell
 *
 */
public interface SubEquipmentConfigHandler {

  /**
   * Creates a SubEquipment in the C2MON server.
   *
   * @param element contains details of the SubEquipment
   * @return change indicating a DAQ restart
   * @throws IllegalAccessException
   */
  List<ProcessChange> createSubEquipment(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Updates an SubEquipment in the C2MON server.
   *
   * @param subEquipmentId the id of the Equipment to update
   * @param elementProperties details of the fields to modify
   * @return a list of changes to send to the DAQ layer
   */
  List<ProcessChange> updateSubEquipment(Long subEquipmentId, Properties elementProperties) throws IllegalAccessException;

  /**
   * Removes an SubEquipment from the C2MON server.
   *
   * @param subEquipmentId the id of the Equipment to remove
   * @param subEquipmentReport the report for this event;
   *         is passed as parameter so cascaded actions can attach subreports
   * @return always returns a change object requiring restart (remove not supported on DAQ layer so far)
   */
  List<ProcessChange> removeSubEquipment(Long subEquipmentId, ConfigurationElementReport subEquipmentReport);

}
