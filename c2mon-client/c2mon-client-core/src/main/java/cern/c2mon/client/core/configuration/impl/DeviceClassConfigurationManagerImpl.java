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
import cern.c2mon.client.core.configuration.DeviceClassConfigurationManager;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.device.DeviceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.validateIsCreate;
import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.validateIsUpdate;

@Service("deviceClassConfigurationManager")
public class DeviceClassConfigurationManagerImpl implements DeviceClassConfigurationManager {

  private ConfigurationRequestSender configurationRequestSender;

  @Autowired
  DeviceClassConfigurationManagerImpl(ConfigurationRequestSender configurationRequestSender) {
    this.configurationRequestSender = configurationRequestSender;
  }

  public ConfigurationReport createDeviceClass(String deviceClassName) {

    return createDeviceClass(DeviceClass.create(deviceClassName).build());
  }

  public ConfigurationReport createDeviceClass(DeviceClass deviceClass) {

    List<DeviceClass> deviceClasses = new ArrayList<>();
    deviceClasses.add(deviceClass);

    validateIsCreate(deviceClasses);

    Configuration config = new Configuration();
    config.setEntities(deviceClasses);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport updateDeviceClass(DeviceClass deviceClass) {

    List<DeviceClass> deviceClasses = new ArrayList<>();
    deviceClasses.add(deviceClass);

    validateIsUpdate(deviceClasses);

    Configuration config = new Configuration();
    config.setEntities(deviceClasses);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeDeviceClassById(Long id) {

    DeviceClass deleteDeviceClass = new DeviceClass();
    deleteDeviceClass.setId(id);
    deleteDeviceClass.setDeleted(true);

    Configuration config = new Configuration();
    config.setEntities(Collections.singletonList(deleteDeviceClass));

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeDeviceClass(String name) {

    DeviceClass deleteDeviceClass = new DeviceClass();
    deleteDeviceClass.setName(name);
    deleteDeviceClass.setDeleted(true);

    Configuration config = new Configuration();
    config.setEntities(Collections.singletonList(deleteDeviceClass));

    return configurationRequestSender.applyConfiguration(config, null);
  }


}
