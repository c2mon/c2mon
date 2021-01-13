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
import cern.c2mon.client.core.configuration.DeviceConfigurationManager;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.device.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.validateIsCreate;
import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.validateIsUpdate;


/**
 * Implementation of the DeviceConfigurationManager which allows to apply create, update and delete configurations for Devices.
 */
@Service("deviceConfigurationManager")
public class DeviceConfigurationManagerImpl implements DeviceConfigurationManager {

  private ConfigurationRequestSender configurationRequestSender;

  @Autowired
  DeviceConfigurationManagerImpl(ConfigurationRequestSender configurationRequestSender) {
    this.configurationRequestSender = configurationRequestSender;
  }

  public ConfigurationReport createDevice(String deviceName, String deviceClassName) {

    return createDevice(Device.create(deviceName, deviceClassName).build());
  }

  public ConfigurationReport createDevice(String deviceName, long deviceClassId) {

    return createDevice(Device.create(deviceName, deviceClassId).build());
  }

  public ConfigurationReport createDevice(Device device) {

    List<Device> devices = new ArrayList<>();
    devices.add(device);

    validateIsCreate(devices);

    Configuration config = new Configuration();
    config.setEntities(devices);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeDeviceById(Long id) {

    Device deleteDevice = new Device();
    deleteDevice.setId(id);
    deleteDevice.setDeleted(true);

    Configuration config = new Configuration();
    config.setEntities(Collections.singletonList(deleteDevice));

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeDevice(String name) {

    Device deleteDevice = new Device();
    deleteDevice.setName(name);
    deleteDevice.setDeleted(true);

    Configuration config = new Configuration();
    config.setEntities(Collections.singletonList(deleteDevice));

    return configurationRequestSender.applyConfiguration(config, null);
  }

}
