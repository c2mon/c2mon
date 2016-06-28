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
import cern.c2mon.client.core.configuration.SubEquipmentConfigurationManager;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Franz Ritter
 */
@Service("subEquipmentConfigurationManager")
public class SubEquipmentConfigurationManagerImpl implements SubEquipmentConfigurationManager {

  private ConfigurationRequestSender configurationRequestSender;

  @Autowired
  SubEquipmentConfigurationManagerImpl(ConfigurationRequestSender configurationRequestSender) {
    this.configurationRequestSender = configurationRequestSender;
  }

  @Override
  public ConfigurationReport createSubEquipment(String equipmentName, String name, String handlerClass) {

    return createSubEquipment(equipmentName, SubEquipment.create(name).build());

  }

  @Override
  public ConfigurationReport createSubEquipment(String equipmentName, SubEquipment subEquipment) {

    List<SubEquipment> equipments = new ArrayList<>();
    equipments.add(subEquipment);

    return createSubEquipment(equipmentName, equipments);
  }

  @Override
  public ConfigurationReport createSubEquipment(String equipmentName, List<SubEquipment> subEquipments) {

    // validate configuration
    validateIsCreate(subEquipments);

    // Set parent Ids to the configuration
    for (SubEquipment equipment : subEquipments) {
      equipment.setParentEquipmentName(equipmentName);
    }

    // set fields for the server
    Configuration config = new Configuration();
    config.setEntities(subEquipments);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport updateSubEquipment(SubEquipment subEquipment) {

    List<SubEquipment> dummySubEquipmentList = new ArrayList<>();
    dummySubEquipmentList.add(subEquipment);

    return updateSubEquipment(dummySubEquipmentList);
  }

  @Override
  public ConfigurationReport updateSubEquipment(List<SubEquipment> subEquipments) {

    // validate the Configuration object
    validateIsUpdate(subEquipments);

    Configuration config = new Configuration();
    config.setEntities(subEquipments);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeSubEquipmentById(Long id) {

    Set<Long> deleteSubEquipment = new HashSet<>();
    deleteSubEquipment.add(id);

    return removeSubEquipmentById(deleteSubEquipment);
  }

  @Override
  public ConfigurationReport removeSubEquipmentById(Set<Long> ids) {

    List<SubEquipment> equipmentsToDelete = new ArrayList<>();

    for (Long id : ids) {
      SubEquipment deleteSubEquipment = new SubEquipment();
      deleteSubEquipment.setId(id);
      deleteSubEquipment.setDeleted(true);

      equipmentsToDelete.add(deleteSubEquipment);
    }

    Configuration config = new Configuration();
    config.setEntities(equipmentsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeSubEquipment(String subEquipmentName) {

    Set<String> deleteSubEquipment = new HashSet<>();
    deleteSubEquipment.add(subEquipmentName);

    return removeSubEquipment(deleteSubEquipment);
  }

  @Override
  public ConfigurationReport removeSubEquipment(Set<String> subEquipmentNames) {

    List<SubEquipment> equipmentsToDelete = new ArrayList<>();

    for (String equipmentName : subEquipmentNames) {
      SubEquipment deleteSubEquipment = new SubEquipment();
      deleteSubEquipment.setName(equipmentName);
      deleteSubEquipment.setDeleted(true);

      equipmentsToDelete.add(deleteSubEquipment);
    }

    Configuration config = new Configuration();
    config.setEntities(equipmentsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }
}
