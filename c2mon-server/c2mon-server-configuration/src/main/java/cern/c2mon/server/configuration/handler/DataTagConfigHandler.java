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

import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.configuration.handler.impl.TagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * Bean managing configuration updates to C2MON DataTags.
 * 
 * @author Mark Brightwell
 *
 */
public interface DataTagConfigHandler extends TagConfigHandler<DataTag> {

  /**
   * Creates a DataTag in the C2MON server.
   * 
   * @param element contains details of the Tag
   * @return creation event to send to the DAQ layer
   * @throws IllegalAccessException
   */
  ProcessChange createDataTag(ConfigurationElement element) throws IllegalAccessException;
  
  /**
   * Updates a DataTag in the C2MON server. Always results in a event being
   * send to the DAQ layer.
   * @param id the id of the Tag to update
   * @param elementProperties details of the fields to modify
   * @return the change to send to the DAQ layer
   */
  ProcessChange updateDataTag(Long id, Properties elementProperties);
  
  /**
   * Removes a DataTag from the C2MON server.
   * @param id the id of the Tag to remove
   * @param tagReport the report for this event; 
   *         is passed as parameter so cascaded action can attach subreports
   * @return a list of changes to send to the DAQ layer
   */
  ProcessChange removeDataTag(Long id, ConfigurationElementReport tagReport);
}
