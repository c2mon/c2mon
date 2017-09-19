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
package cern.c2mon.server.cache.loader.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.EquipmentMapper;
import cern.c2mon.server.cache.loader.EquipmentDAO;
import cern.c2mon.server.cache.loader.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;

/**
 * Equipment DAO implementation.
 *
 * @author Mark Brightwell
 */
//TODO: refer a name
@Service("equipmentDAORef")
public class EquipmentDAOImpl extends AbstractDefaultLoaderDAO<Equipment> implements EquipmentDAO {

  private EquipmentMapper equipmentMapper;

  @Autowired
  public EquipmentDAOImpl(EquipmentMapper equipmentMapper) {
    super(550, equipmentMapper);
    this.equipmentMapper = equipmentMapper;
  }

  @Override
  public void updateConfig(Equipment equipment) {
    equipmentMapper.updateEquipmentConfig((EquipmentCacheObject) equipment);
  }

  @Override
  public void deleteItem(Long id) {
    equipmentMapper.deleteEquipment(id);
  }

  @Override
  public void insert(Equipment equipment) {
    equipmentMapper.insertEquipment((EquipmentCacheObject) equipment);
  }

  @Override
  protected Equipment doPostDbLoading(Equipment item) {
    //do nothing for this cache
    return item;
  }

  @Override
  public Long getIdByName(String name) {
    return equipmentMapper.getIdByName(name);
  }
}
