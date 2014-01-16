/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.cache;

import java.util.Collection;

import cern.c2mon.server.cache.equipment.CommonEquipmentFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.equipment.Equipment;

/**
 * Interface of the bean used to interact with the EquipmentCacheObject.
 * 
 * @author Mark Brightwell
 *
 */
public interface EquipmentFacade extends CommonEquipmentFacade<Equipment> {
  
  /**
   * Returns a collection of the ids of all DataTags
   * registered with this equipment (not control tags).
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
   * Adds the DataTag to the list of DataTags attached to this equipment.
   * 
   * @param equipmentId equipment object in cache
   * @param dataTagId the id of the datatag to add
   */
  void addTagToEquipment(Long equipmentId, Long dataTagId);
  
  /**
   * Detaches a tag from the equipment. Throws {@link CacheElementNotFoundException}
   * if the Equipment does not exist.
   * @param equipmentId some Equipment id
   * @param dataTagId id of the Tag to remove from the Equipment
   */
  void removeTagFromEquipment(Long equipmentId, Long dataTagId);

  /**
   * Adds the equipment to list of those under the process. The Process
   * cache object is updated (write lock at Process level).
   * 
   * @param equipmentId Equipment id
   * @param process Process processId
   */
  void addEquipmentToProcess(Long equipmentId, Long processId);

  /**
   * Removes the command from the list of commands for this Equipment.
   * @param equipmentId the equipment id
   * @param commandId the command id
   */
  void removeCommandFromEquipment(final Long equipmentId, final Long commandId);

  /**
   * Adds the command to the list of commands for this Equipment.
   * @param equipmentId the equipment id
   * @param commandId the command id
   */
  void addCommandToEquipment(Long equipmentId, Long commandId);
  
  
}
