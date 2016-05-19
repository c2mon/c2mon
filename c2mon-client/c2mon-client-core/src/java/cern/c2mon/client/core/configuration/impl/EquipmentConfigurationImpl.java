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
import cern.c2mon.client.core.configuration.EquipmentConfiguration;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.*;

/**
 * @author Franz Ritter
 */
@Service("equipmentConfiguration")
public class EquipmentConfigurationImpl implements EquipmentConfiguration {

  private ConfigurationRequestSender configurationRequestSender;

  @Autowired
  EquipmentConfigurationImpl(ConfigurationRequestSender configurationRequestSender) {
    this.configurationRequestSender = configurationRequestSender;
  }

  @Override
  public ConfigurationReport createEquipment(Long processId, String name, String handlerClass) {

    return createEquipment(processId, Equipment.create(name, handlerClass).build());
  }

  @Override
  public ConfigurationReport createEquipment(Long processId, Equipment equipment) {

    List<Equipment> equipments = new ArrayList<>();
    equipments.add(equipment);

    return createEquipments(processId, equipments);
  }

  @Override
  public ConfigurationReport createEquipments(Long processId, List<Equipment> equipments) {

    // validate configuration
    validateIsCreate(equipments);

    // Set parent Ids to the configuration
    for (Equipment equipment : equipments) {
      equipment.setParentProcessId(processId);
    }

    // set fields for the server
    Configuration config = new Configuration();
    config.setConfigurationItems(equipments);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport createEquipment(String processName, String name, String handlerClass) {

    return createEquipment(processName, Equipment.create(name, handlerClass).build());

  }

  @Override
  public ConfigurationReport createEquipment(String processName, Equipment equipment) {

    List<Equipment> equipments = new ArrayList<>();
    equipments.add(equipment);

    return createEquipments(processName, equipments);
  }

  @Override
  public ConfigurationReport createEquipments(String processName, List<Equipment> equipments) {

    // validate configuration
    validateIsCreate(equipments);

    // Set parent Ids to the configuration
    for (Equipment equipment : equipments) {
      equipment.setParentProcessName(processName);
    }

    // set fields for the server
    Configuration config = new Configuration();
    config.setConfigurationItems(equipments);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport updateEquipment(Equipment equipment) {

    List<Equipment> dummyEquipmentList = new ArrayList<>();
    dummyEquipmentList.add(equipment);

    return updateEquipments(dummyEquipmentList);
  }

  @Override
  public ConfigurationReport updateEquipments(List<Equipment> equipments) {

    // validate the Configuration object
    validateIsUpdate(equipments);

    Configuration config = new Configuration();
    config.setConfigurationItems(equipments);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeEquipment(Long id) {

    List<Long> deleteEquipment = new ArrayList<>();
    deleteEquipment.add(id);

    return removeEquipments(deleteEquipment);
  }

  @Override
  public ConfigurationReport removeEquipments(List<Long> ids) {

    List<Equipment> equipmentsToDelete = new ArrayList<>();

    for (Long id : ids) {
      equipmentsToDelete.add(Equipment.builder().id(id).deleted(true).build());
    }

    Configuration config = new Configuration();
    config.setConfigurationItems(equipmentsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeEquipment(String equipmentName) {

    List<String> deleteEquipment = new ArrayList<>();
    deleteEquipment.add(equipmentName);

    return removeEquipmentsByName(deleteEquipment);
  }

  @Override
  public ConfigurationReport removeEquipmentsByName(List<String> equipmentNames) {

    List<Equipment> equipmentsToDelete = new ArrayList<>();

    for (String equipmentName : equipmentNames) {
      equipmentsToDelete.add(Equipment.builder().name(equipmentName).deleted(true).build());
    }

    Configuration config = new Configuration();
    config.setConfigurationItems(equipmentsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }

}
