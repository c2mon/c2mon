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

import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.configuration.impl.ProcessChange;

import java.util.List;
import java.util.Properties;

/**
 * Common parts of transacted config bean for Equipment/Sub-equipment. 
 * 
 * @author Mark Brightwell
 *
 * @param <T> type of abstract equipment
 */
// TODO (Alex) Eradicate this!
public interface CommonEquipmentConfigTransacted<T extends AbstractEquipment> {

  /**
   * Transacted method for creating sub-equipment.
   * @param subEquipment ref to sub-equipment
   * @param properties details of update
   * @return change event for DAQ
   * @throws IllegalAccessException
   */
  List<ProcessChange> update(T abstractEquipment, Properties properties) throws IllegalAccessException;
  
}
