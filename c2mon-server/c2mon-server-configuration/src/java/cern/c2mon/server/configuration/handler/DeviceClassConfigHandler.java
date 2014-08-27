/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.server.configuration.handler;

import java.util.Properties;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * Bean managing configuration updates to C2MON DeviceClasses.
 *
 * @author Justin Lewis Salmon
 */
public interface DeviceClassConfigHandler {

  /**
   * Create a DeviceClass object in the C2MON server.
   *
   * @param element contains details of the DeviceClass
   *
   * @return change to be sent to the DAQ layer (none in this case)
   * @throws IllegalAccessException
   */
  ProcessChange createDeviceClass(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Update a DeviceClass object in the C2MON server.
   *
   * @param id the ID of the DeviceClass to update
   * @param elementProperties details of the fields to modify
   *
   * @return change to be sent to the DAQ layer (none in this case)
   */
  ProcessChange updateDeviceClass(Long id, Properties elementProperties);

  /**
   * Remove a DeviceClass object from the C2MON server.
   *
   * @param id the ID of the DeviceClass to remove
   * @param elementReport the report for this event; is passed as parameter so
   *          cascaded action can attach subreports
   *
   * @return change to be sent to the DAQ layer (none in this case)
   */
  ProcessChange removeDeviceClass(Long id, ConfigurationElementReport elementReport);
}
