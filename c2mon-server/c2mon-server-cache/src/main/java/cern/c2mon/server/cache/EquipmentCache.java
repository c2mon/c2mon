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
package cern.c2mon.server.cache;

import cern.c2mon.server.common.equipment.Equipment;

/**
 * The module public interface that should be used to access the Equipment
 * in the server cache. 
 * 
 * <p>It provides methods for retrieving references to the objects in the
 * cache, which may be accessed by other threads concurrently. To guarantee 
 * exclusive access the thread must synchronize on the Equipment object in
 * the cache.
 * 
 * <p>Notice that Equipments cannot share the same CommFault or AliveTag ids,
 * although this is not enforced in the cache DB. The loading of the CommFault
 * and Alive cache will then fail as multiple entries will be returned!
 * 
 * @author Mark Brightwell
 *
 */
public interface EquipmentCache extends C2monCacheWithListeners<Long, Equipment> {
  
  String cacheInitializedKey = "c2mon.cache.equipment.initialized";
  
}
