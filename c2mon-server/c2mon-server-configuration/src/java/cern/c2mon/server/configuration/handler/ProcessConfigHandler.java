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

import org.springframework.transaction.UnexpectedRollbackException;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * Bean managing configuration updates to C2MON DataTags.
 * 
 * @author Mark Brightwell
 *
 */
public interface ProcessConfigHandler {

  /**
   * Creates a Process in the C2MON server.
   * 
   * @param element contains details of the Process
   * @return a ProcessChange containing only the id for restarting
   * @throws IllegalAccessException
   */
  ProcessChange createProcess(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Updates a Process in the C2MON server.
   * 
   * @param processId the id of the Process to update
   * @param elementProperties details of the fields to modify
   * @return a change to send to the DAQ layer
   */
  ProcessChange updateProcess(Long processId, Properties elementProperties) throws IllegalAccessException;

  /**
   * Removes an Process from the C2MON server.
   * 
   * @param processId the id of the Process to remove
   * @param processReport the report for this event; 
   *         is passed as parameter so cascaded action can attach subreports
   * @return change event indicating DAQ restart is necessary (in fact shutdown in this case)
   */
  ProcessChange removeProcess(Long processId, ConfigurationElementReport processReport);

  /**
   * Removes an equipment reference from the process that contains it.
   * @param equipmentId the equipment to remove
   * @param processId the process to remove the equipment reference from
   * @throws UnexpectedRollbackException if this operation fails
   */
  void removeEquipmentFromProcess(Long equipmentId, Long processId);

}
