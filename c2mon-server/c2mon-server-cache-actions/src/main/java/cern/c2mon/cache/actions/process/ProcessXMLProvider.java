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
package cern.c2mon.cache.actions.process;

import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;

/**
 * Interface to service for obtaining the XML representation
 * of a Process configuration.
 *
 * @author Mark Brightwell
 *
 */
public interface ProcessXMLProvider {

  /**
   * Throws CacheElementNotFound exception if the equipment
   * could not be located in the cache.
   *
   * @param process the process for which to get the configuration
   * @return the XML as String
   */
  String getProcessConfigXML(Process process);

  /**
   * As for <code>getProcessConfigXML(Process process)</code> but using name as parameter.
   * @param processName name of the process
   * @return the XML as String
   */
  String getProcessConfigXML(String processName);

  /**
   * Generate the DAQ configuration XML structure for an equipment unit.
   * This method is called by the ProcessFacade when a DAQ starts up and
   * requests its configuration. The XML structure returned by the method
   * is part of the configuration XML message sent to the DAQ.
   *
   * <p>Throws a CacheElementNotFound exception if the Equipment cannot be located
   * in the cache.
   *
   * <p>Call within block synchronized on the parent Process to avoid
   * changes to the configuration while this method is called.
   *
   * @param id the id of the Equipment
   * @return the XML as a String
   */
  String getEquipmentConfigXML(Long id);

  /**
   * Generate the DAQ configuration XML structure for all SubEquipment units
   * belonging to an equipment..
   *
   * Call within block synchronized on the parent Process to avoid changes to
   * the configuration while this method is called.
   *
   * @param id the Id of the Equipment cache object
   * @return a reference to the EquipmentCacheObject in the cache.
   */
  String getSubEquipmentConfigXML(Long id);

  /**
   * Generate the DAQ configuration XML structure for a single SubEquipment
   * unit.
   *
   * Call within block synchronized on the parent Process to avoid changes
   * to the configuration while this method is called.
   *
   * @param id the Id of the Equipment cache object
   * @return a reference to the EquipmentCacheObject in the cache.
   */
  String getSubEquipmentConfigXML(SubEquipmentCacheObject subEquipment);
}
