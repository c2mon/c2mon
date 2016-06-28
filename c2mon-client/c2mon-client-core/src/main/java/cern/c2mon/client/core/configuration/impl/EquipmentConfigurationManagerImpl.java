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
package cern.c2mon.client.core.configuration.impl;

import cern.c2mon.client.core.configuration.ConfigurationRequestSender;
import cern.c2mon.client.core.configuration.EquipmentConfigurationManager;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.*;

/**
 * @author Franz Ritter
 */
@Service("equipmentConfigurationManager")
public class EquipmentConfigurationManagerImpl implements EquipmentConfigurationManager {

  private ConfigurationRequestSender configurationRequestSender;

  @Autowired
  EquipmentConfigurationManagerImpl(ConfigurationRequestSender configurationRequestSender) {
    this.configurationRequestSender = configurationRequestSender;
  }

  @Override
  public ConfigurationReport createEquipment(String processName, String name, String handlerClass) {

    return createEquipment(processName, Equipment.create(name, handlerClass).build());

  }

  @Override
  public ConfigurationReport createEquipment(String processName, Equipment equipment) {

    List<Equipment> equipments = new ArrayList<>();
    equipments.add(equipment);

    return createEquipment(processName, equipments);
  }

  @Override
  public ConfigurationReport createEquipment(String processName, List<Equipment> equipments) {

    // validate configuration
    validateIsCreate(equipments);

    // Set parent Ids to the configuration
    for (Equipment equipment : equipments) {
      equipment.setParentProcessName(processName);
    }

    // set fields for the server
    Configuration config = new Configuration();
    config.setEntities(equipments);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport updateEquipment(Equipment equipment) {

    List<Equipment> dummyEquipmentList = new ArrayList<>();
    dummyEquipmentList.add(equipment);

    return updateEquipment(dummyEquipmentList);
  }

  @Override
  public ConfigurationReport updateEquipment(List<Equipment> equipments) {

    // validate the Configuration object
    validateIsUpdate(equipments);

    Configuration config = new Configuration();
    config.setEntities(equipments);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeEquipmentById(Long id) {

    Set<Long> deleteEquipment = new HashSet<>();
    deleteEquipment.add(id);

    return removeEquipmentById(deleteEquipment);
  }

  @Override
  public ConfigurationReport removeEquipmentById(Set<Long> ids) {

    List<Equipment> equipmentsToDelete = new ArrayList<>();

    for (Long id : ids) {
      Equipment deleteEquipment = new Equipment();
      deleteEquipment.setId(id);
      deleteEquipment.setDeleted(true);

      equipmentsToDelete.add(deleteEquipment);
    }

    Configuration config = new Configuration();
    config.setEntities(equipmentsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeEquipment(String equipmentName) {

    Set<String> deleteEquipment = new HashSet<>();
    deleteEquipment.add(equipmentName);

    return removeEquipment(deleteEquipment);
  }

  @Override
  public ConfigurationReport removeEquipment(Set<String> equipmentNames) {

    List<Equipment> equipmentsToDelete = new ArrayList<>();

    for (String equipmentName : equipmentNames) {
      Equipment deleteEquipment = new Equipment();
      deleteEquipment.setName(equipmentName);
      deleteEquipment.setDeleted(true);

      equipmentsToDelete.add(deleteEquipment);
    }

    Configuration config = new Configuration();
    config.setEntities(equipmentsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }

}
