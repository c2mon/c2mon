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
package cern.c2mon.server.configuration.handler.transacted;

import java.util.List;

import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 *  For internal use only. Allows use of Spring AOP for transaction management.
 *
 * @author Mark Brightwell
 *
 */
public interface SubEquipmentConfigTransacted extends CommonEquipmentConfigTransacted<SubEquipment> {

  /**
   * Transacted method for removing subequipment.
   *
   * @param subEquipment ref to sub-equipment
   * @param subEquipmentReport report
   * @return list of changes for DAQ
   */
  List<ProcessChange> doRemoveSubEquipment(SubEquipment subEquipment, ConfigurationElementReport subEquipmentReport);

  /**
   * Transacted method for creating subequipment.
   * @param element config details
   * @return change for DAQ
   * @throws IllegalAccessException
   */
  List<ProcessChange> doCreateSubEquipment(ConfigurationElement element) throws IllegalAccessException;


}
