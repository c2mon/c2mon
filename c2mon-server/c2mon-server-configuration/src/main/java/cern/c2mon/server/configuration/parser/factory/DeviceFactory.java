/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.server.configuration.parser.factory;

import cern.c2mon.server.cache.DeviceCache;
import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.device.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Elisabeth Stockinger
 */
@Service
@Slf4j
public class DeviceFactory extends EntityFactory<Device> {

  private final DeviceCache deviceCache;
  private final DeviceClassCache deviceClassCache;
  private final SequenceDAO sequenceDAO;

  @Autowired
  public DeviceFactory(DeviceCache deviceCache, DeviceClassCache deviceClassCache, SequenceDAO sequenceDAO) {
    super(deviceCache);
    this.deviceCache = deviceCache;
    this.sequenceDAO = sequenceDAO;
    this.deviceClassCache = deviceClassCache;
  }


  @Override
  public List<ConfigurationElement> createInstance(Device entity) {
    List<ConfigurationElement> configurationElements = new ArrayList<>();

    // Build the process configuration element. This also sets the device class id
    ConfigurationElement createDevice = doCreateInstance(entity);
    configurationElements.add(createDevice);

    return configurationElements;
  }


  @Override
  Long getId(Device entity) {
    if (entity.getId() != null) {
      return entity.getId();
    }
    return loadIdFromCache(entity);
  }

  @Override
  Long createId(Device entity) {
    if (entity.getName() != null && loadIdFromCache(entity) != null) {
      throw new ConfigurationParseException("Error creating deviceClass " + entity.getName() + ": " +
              "Name already exists");
    } else {
      return entity.getId() != null ? entity.getId() : sequenceDAO.getNextDeviceId();
    }
  }

  @Override
  ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.DEVICE;
  }

  private Long loadIdFromCache(Device entity) {
    Long deviceClassId = entity.getClassId() == null ?
            deviceClassCache.getDeviceClassIdByName(entity.getClassName()) :
            entity.getId();
    if (deviceClassId == null) {
      throw new ConfigurationParseException("Error creating device " + entity.getName() + ": " +
              "No deviceClass with name " + entity.getClassName() + " exists.");
    }
    entity.setClassId(deviceClassId);
    try {
      List<cern.c2mon.server.common.device.Device> devices = deviceCache.getByDeviceClassId(deviceClassId);
      return devices.stream()
              .filter(d -> d.getName().equalsIgnoreCase(entity.getName()))
              .findFirst()
              .map(cern.c2mon.server.common.device.Device::getId)
              .orElse(null);
    } catch (CacheElementNotFoundException e) {
      return null;
    }
  }
}