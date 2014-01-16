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

import cern.c2mon.server.cache.equipment.CommonEquipmentFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.subequipment.SubEquipment;

/**
 * Facade object with business methods for the SubEquipment
 * cache object.
 * 
 * @author Mark Brightwell
 *
 */
public interface SubEquipmentFacade extends CommonEquipmentFacade<SubEquipment> {
  
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
   * @param subEquipmentId id of the SubEquipment
   * @param equipmentId id of the parent Equipment
   */
  void addSubEquipmentToEquipment(Long subEquipmentId, Long equipmentId);
}
