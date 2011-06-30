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
package cern.c2mon.server.configuration.handler;

import java.util.List;
import java.util.Properties;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;

/**
 * Bean managing configuration updates to C2MON Equipment.
 * 
 * @author Mark Brightwell
 *
 */
public interface EquipmentConfigHandler {

  /**
   * Creates an Equipment in the C2MON server.
   * 
   * @param element contains details of the Equipment
   * @throws IllegalAccessException
   */
  void createEquipment(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Updates an Equipment in the C2MON server.
   * @param equipmentId the id of the Equipment to update
   * @param elementProperties details of the fields to modify
   * @return a list of changes to send to the DAQ layer
   */
  List<ProcessChange> updateEquipment(Long equipmentId, Properties elementProperties) throws IllegalAccessException;

  /**
   * Removes an Equipment from the C2MON server.
   * @param equipmentId the id of the Equipment to remove
   * @param equipmentReport the report for this event; 
   *         is passed as parameter so cascaded actions can attach subreports
   */
  void removeEquipment(Long equipmentId, ConfigurationElementReport equipmentReport);

}
