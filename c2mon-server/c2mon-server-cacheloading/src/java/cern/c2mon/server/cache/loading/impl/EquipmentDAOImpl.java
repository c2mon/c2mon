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
package cern.c2mon.server.cache.loading.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.EquipmentMapper;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.cache.loading.common.AbstractBatchLoaderDAO;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;

/**
 * Equipment DAO implementation.
 * 
 * @author Mark Brightwell
 *
 */
@Service("equipmentDAO")
public class EquipmentDAOImpl extends AbstractBatchLoaderDAO<Equipment> implements EquipmentDAO {
  
  private EquipmentMapper equipmentMapper;

  @Autowired
  public EquipmentDAOImpl(EquipmentMapper equipmentMapper) {
    super(equipmentMapper);
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
  

}
