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
package cern.c2mon.cache.actions.subequipment;

import cern.c2mon.cache.api.service.CommonEquipmentOperations;

import java.util.Collection;

/**
 * Facade object with business methods for the SubEquipment
 * cache object.
 *
 * @author Mark Brightwell
 */
public interface SubEquipmentOperations extends CommonEquipmentOperations {

  /**
   * Returns the Equipment id for a given SubEquipment.
   *
   * <p>Throws a {@link CacheElementNotFoundException} if the Equipment
   * or SubEquipment cannot be located in the cache. Throws a {@link NullPointerException}
   * if the SubEquipment has no parent Id set.
   *
   * @param subEquipmentId the Id of the SubEquipment
   * @return The id of the Equipment object in the cache
   */
  Long getEquipmentIdForSubEquipment(Long subEquipmentId);

  /**
   * Adds the SubEquipment to the list of SubEquipments of the equipment.
   *
   * @param subEquipmentId id of the SubEquipment
   * @param equipmentId    id of the parent Equipment
   */
  void addSubEquipmentToEquipment(Long subEquipmentId, Long equipmentId);

  /**
   * Returns a collection of the ids of all DataTags registered with this
   * subequipment (not control tags).
   *
   * @param id of the subequipment
   * @return the ids in a collection
   */
  Collection<Long> getDataTagIds(Long subEquipmentId);

  /**
   * Removes the SubEquipment from the list of SubEquipments of the Equipment.
   *
   * @param equipmentId    the ID of the equipment
   * @param subEquipmentId the ID of the SubEquipment to remove
   */
  void removeSubEquipmentFromEquipment(Long equipmentId, Long subEquipmentId);
}
