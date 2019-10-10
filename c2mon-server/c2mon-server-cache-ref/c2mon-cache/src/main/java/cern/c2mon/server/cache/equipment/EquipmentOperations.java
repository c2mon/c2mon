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
package cern.c2mon.server.cache.equipment;

import cern.c2mon.cache.api.service.CommonEquipmentOperations;

import java.util.Collection;

/**
 * Interface of the bean used to interact with the EquipmentCacheObject.
 *
 * @author Mark Brightwell
 */
public interface EquipmentOperations extends CommonEquipmentOperations {

  /**
   * Returns a collection of the ids of all DataTags
   * registered with this equipment (not control tags).
   *
   * @param id of the equipment
   * @return the ids in a collection
   */
  Collection<Long> getDataTagIds(Long equipmentId);

  /**
   * Returns all alive tag ids for all equipment currently configured
   * (not including Process alives).
   *
   * <p>Is used for example when reconfiguring, to decide if the control tag
   * needs forwarding to the DAQ layer.
   *
   * @return all equipment alives
   */
  Collection<Long> getEquipmentAlives();

  /**
   * Adds the equipment to list of those under the process. The Process
   * cache object is updated (write lock at Process level).
   *
   * @param equipmentId Equipment id
   * @param process     Process processId
   */
  void addEquipmentToProcess(Long equipmentId, Long processId);

  /**
   * Removes the command from the list of commands for this Equipment.
   *
   * @param equipmentId the equipment id
   * @param commandId   the command id
   */
  void removeCommandFromEquipment(final Long equipmentId, final Long commandId);

  /**
   * Adds the command to the list of commands for this Equipment.
   *
   * @param equipmentId the equipment id
   * @param commandId   the command id
   */
  void addCommandToEquipment(Long equipmentId, Long commandId);


}
