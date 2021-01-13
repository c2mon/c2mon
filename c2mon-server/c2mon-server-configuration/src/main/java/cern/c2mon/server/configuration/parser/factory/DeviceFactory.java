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
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.device.Device;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.client.device.Property;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    ConfigurationElement createDevice = doCreateInstance(entity);
    configurationElements.add(createDevice);

    return configurationElements;
  }


  @Override
  Long getId(Device entity) {
    return entity.getId() != null ? entity.getId() : loadIdFromCache(entity);
  }

  @Override
  Long createId(Device entity) {
    if (entity.getName() != null && loadIdFromCache(entity) != null) {
      throw new ConfigurationParseException("Error creating device " + entity.getName() + ": " +
              "Name already exists");
    } else if (!entity.getDeviceCommands().getDeviceCommands().isEmpty() ||
            !entity.getDeviceProperties().getDeviceProperties().isEmpty()) {
      DeviceClass deviceClass = deviceClassCache.get(entity.getClassId());
      setDevicePropertyIds(entity, deviceClass);
      setDeviceCommandIds(entity, deviceClass);
    }
    return entity.getId() != null ? entity.getId() : sequenceDAO.getNextDeviceId();
  }

  @Override
  ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.DEVICE;
  }

  private Long loadIdFromCache(Device entity) {
    Long deviceClassId = entity.getClassId() == null ?
            deviceClassCache.getDeviceClassIdByName(entity.getClassName()) :
            entity.getClassId();
    if (deviceClassId == null) {
      throw new ConfigurationParseException("Error creating device " + entity.getName() + ": " +
              "No deviceClass with name " + entity.getClassName() + " exists.");
    }
    entity.setClassId(deviceClassId);
    try {
      List<cern.c2mon.server.common.device.Device> devices = deviceCache.getByDeviceClassId(deviceClassId);
      if (devices != null) {
        Optional<cern.c2mon.server.common.device.Device> existingDevice = devices.stream()
                .filter(d -> d.getName().equalsIgnoreCase(entity.getName()))
                .findFirst();
        if (existingDevice.isPresent()) {
          return existingDevice.get().getId();
        }
      }
    } catch (CacheElementNotFoundException ignored) {
      // new device, not in cache
    }
    return null;
  }

  private void setDevicePropertyIds(Device entity, DeviceClass deviceClass) {
    // fetch associated property instead of ID so that we can later associate field ID and device field name
    for (DeviceProperty deviceProperty : entity.getDeviceProperties().getDeviceProperties()) {
      Optional<Property> property = deviceClass.getProperties()
              .stream()
              .filter(p -> p.getName().equals(deviceProperty.getName()))
              .findFirst();

      if (!property.isPresent() || property.get().getId() == null) {
        throw new ConfigurationParseException("Error creating device " + entity.getName() + ": " +
                "DeviceProperty \"" + deviceProperty.getName() + "\" must refer to a property defined in parent class");
      } else if (deviceProperty.getId() == null) {
        deviceProperty.setId(property.get().getId());
      }

      for (DeviceProperty deviceField : deviceProperty.getFields().values()) {
        Optional<Property> field = property.get().getFields().stream()
                .filter(p -> p.getName().equals(deviceField.getName()))
                .findFirst();
        if (!field.isPresent()) {
          throw new ConfigurationParseException("Error creating device " + entity.getName() + ": " +
                  "DeviceProperty \"" + deviceProperty.getName() + "\" must refer to a property defined in parent class");
        } else {
          deviceField.setId(field.get().getId());
        }
      }
    }
  }

  private void setDeviceCommandIds(Device entity, DeviceClass deviceClass) {
    for (DeviceCommand deviceCommand : entity.getDeviceCommands().getDeviceCommands()) {
      if (deviceCommand.getId() == null) {
        Long commandId = deviceClass.getCommandId(deviceCommand.getName());
        if (commandId == null) {
          throw new ConfigurationParseException("Error creating device " + entity.getName() + ": " +
                  "deviceCommand \"" + deviceCommand.getName() + "\" must refer to a command defined in parent class");
        }
        deviceCommand.setId(commandId);
      }
    }
  }
}