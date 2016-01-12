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

import java.util.Properties;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * Bean managing configuration updates to C2MON Devices.
 *
 * @author Justin Lewis Salmon
 */
public interface DeviceConfigHandler {

  /**
   * Create a Device object in the C2MON server.
   *
   * @param element contains details of the Device
   *
   * @return change to be sent to the DAQ layer (none in this case)
   * @throws IllegalAccessException
   */
  ProcessChange createDevice(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Update a Device object in the C2MON server.
   *
   * @param id the ID of the Device to update
   * @param elementProperties details of the fields to modify
   *
   * @return change to be sent to the DAQ layer (none in this case)
   */
  ProcessChange updateDevice(Long id, Properties elementProperties);

  /**
   * Remove a Device object from the C2MON server.
   *
   * @param id the ID of the Device to remove
   * @param elementReport the report for this event; is passed as parameter so
   *          cascaded action can attach subreports
   *
   * @return change to be sent to the DAQ layer (none in this case)
   */
  ProcessChange removeDevice(Long id, ConfigurationElementReport elementReport);
}
