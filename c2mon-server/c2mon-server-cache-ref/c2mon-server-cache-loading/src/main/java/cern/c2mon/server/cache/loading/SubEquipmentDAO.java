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
package cern.c2mon.server.cache.loading;

import cern.c2mon.server.common.exception.SubEquipmentException;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;

import java.util.List;

/**
 * SubEquipment DAO interface.
 *
 * @author Mark Brightwell
 */
public interface SubEquipmentDAO extends CacheLoaderDAO<SubEquipment>, ConfigurableDAO<SubEquipment> {

  List<SubEquipment> getSubEquipmentsByEquipment(Long equipmentId) throws SubEquipmentException;

  SubEquipment getSubEquipmentById(Long subEquipmentId) throws SubEquipmentException;

  void create(SubEquipmentCacheObject subEquipment) throws SubEquipmentException;

  Long getIdByName(String name);

}
