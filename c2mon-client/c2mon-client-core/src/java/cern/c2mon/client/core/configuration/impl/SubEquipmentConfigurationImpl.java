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
import cern.c2mon.client.core.configuration.SubEquipmentConfiguration;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fritter on 13/05/16.
 */
@Service("subEquipmentConfiguration")
public class SubEquipmentConfigurationImpl implements SubEquipmentConfiguration {

  private ConfigurationRequestSender configurationRequestSender;

  @Autowired
  SubEquipmentConfigurationImpl(ConfigurationRequestSender configurationRequestSender) {
    this.configurationRequestSender = configurationRequestSender;
  }


  @Override
  public ConfigurationReport createSubEquipment(Long equipmentId, String name, String handlerClass) {

    return createSubEquipment(equipmentId, SubEquipment.create(name, handlerClass).build());
  }

  @Override
  public ConfigurationReport createSubEquipment(Long equipmentId, SubEquipment subEquipment) {

    List<SubEquipment> equipments = new ArrayList<>();
    equipments.add(subEquipment);

    return createSubEquipments(equipmentId, equipments);
  }

  @Override
  public ConfigurationReport createSubEquipments(Long equipmentId, List<SubEquipment> subEquipments) {

    // validate configuration
    validateIsCreate(subEquipments);

    // Set parent Ids to the configuration
    for (SubEquipment equipment : subEquipments) {
      equipment.setParentEquipmentId(equipmentId);
    }

    // set fields for the server
    Configuration config = new Configuration();
    config.setConfigurationItems(subEquipments);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport createSubEquipment(String equipmentName, String name, String handlerClass) {

    return createSubEquipment(equipmentName, SubEquipment.create(name, handlerClass).build());

  }

  @Override
  public ConfigurationReport createSubEquipment(String equipmentName, SubEquipment subEquipment) {

    List<SubEquipment> equipments = new ArrayList<>();
    equipments.add(subEquipment);

    return createSubEquipments(equipmentName, equipments);
  }

  @Override
  public ConfigurationReport createSubEquipments(String equipmentName, List<SubEquipment> subEquipments) {

    // validate configuration
    validateIsCreate(subEquipments);

    // Set parent Ids to the configuration
    for (SubEquipment equipment : subEquipments) {
      equipment.setParentEquipmentName(equipmentName);
    }

    // set fields for the server
    Configuration config = new Configuration();
    config.setConfigurationItems(subEquipments);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport updateSubEquipment(SubEquipment subEquipment) {

    List<SubEquipment> dummySubEquipmentList = new ArrayList<>();
    dummySubEquipmentList.add(subEquipment);

    return updateSubEquipments(dummySubEquipmentList);
  }

  @Override
  public ConfigurationReport updateSubEquipments(List<SubEquipment> subEquipments) {

    // validate the Configuration object
    validateIsUpdate(subEquipments);

    Configuration config = new Configuration();
    config.setConfigurationItems(subEquipments);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeSubEquipment(Long id) {

    List<Long> deleteSubEquipment = new ArrayList<>();
    deleteSubEquipment.add(id);

    return removeSubEquipments(deleteSubEquipment);
  }

  @Override
  public ConfigurationReport removeSubEquipments(List<Long> ids) {

    List<SubEquipment> equipmentsToDelete = new ArrayList<>();

    for (Long id : ids) {
      equipmentsToDelete.add(SubEquipment.builder().id(id).deleted(true).build());
    }

    Configuration config = new Configuration();
    config.setConfigurationItems(equipmentsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeSubEquipment(String subEquipmentName) {

    List<String> deleteSubEquipment = new ArrayList<>();
    deleteSubEquipment.add(subEquipmentName);

    return removeSubEquipmentsByName(deleteSubEquipment);
  }

  @Override
  public ConfigurationReport removeSubEquipmentsByName(List<String> subEquipmentNames) {

    List<SubEquipment> equipmentsToDelete = new ArrayList<>();

    for (String equipmentName : subEquipmentNames) {
      equipmentsToDelete.add(SubEquipment.builder().name(equipmentName).deleted(true).build());
    }

    Configuration config = new Configuration();
    config.setConfigurationItems(equipmentsToDelete);

    return configurationRequestSender.applyConfiguration(config, null);
  }
}
