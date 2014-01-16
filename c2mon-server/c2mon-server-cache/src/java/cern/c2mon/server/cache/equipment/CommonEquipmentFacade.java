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
package cern.c2mon.server.cache.equipment;

import java.util.Map;

import cern.c2mon.server.cache.SupervisedFacade;
import cern.c2mon.server.cache.common.ConfigurableCacheFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.equipment.AbstractEquipment;

/**
 * Common facade methods for Equipment and SubEquipment.
 * 
 * @author Mark Brightwell
 *
 * @param <T> cache object type extending {@link AbstractEquipment}
 */
public interface CommonEquipmentFacade<T extends AbstractEquipment> extends SupervisedFacade<T>, ConfigurableCacheFacade<T> {
  
  /**
   * Returns the Process id for a given Equipment or SubEquipment
   * 
   * <p>Throws a {@link CacheElementNotFoundException} if the SubEquipment, Equipment
   * or Process cannot be located in the cache. Throws a {@link NullPointerException}
   * if the Equipment/SubEquipment has no process/equipment Id set.
   * 
   * @param abstractEquipmentId the Id of the (Sub)Equipment
   * @return The id to the Process object in the cache
   */
  Long getProcessIdForAbstractEquipment(Long abstractEquipmentId);

  /**
   * Dynamically creates a Map ControlTag id -> Equipment id.
   * ControlTags with no associated AbstractEquipment (or associated
   * to a DAQ) are not in the Map.
   * 
   * <p>Is not designed for intensive use (only used during
   * live reconfiguration of ControlTags). If needed for intensive
   * use, change the code to store this information in the cache.
   * 
   * @return the map {ControlTag id -> Equipment id}
   */
  Map<Long, Long> getAbstractEquipmentControlTags();

  /**
   * Removes the commfault tag for this equipment from the 
   * equipment and the commfault cache.
   * @param abstractEquipmentId id of abstract equipment
   * @throws CacheElementNotFoundException if the abstractEquipment cannot be located in the corresponding cache
   */
  void removeCommFault(Long abstractEquipmentId);
  
}
