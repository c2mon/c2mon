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
import cern.c2mon.shared.client.device.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Checks {@link Device} objects for correctness, and creates appropriate {@link ConfigurationElement}s. IDs are
 * created dynamically for new devices. If required the {@link DeviceClass} corresponding to a configued device class
 * name is fetched from the {@link DeviceClassCache}. Similarly, the appropriate {@link Property} and {@link Command}
 * IDs are read from the cache and used as {@link DeviceProperty} and {@link DeviceCommand} when required. The device
 * ID is dynamically generated is necessary.
 *
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
      throw createConfigurationException(entity.getName(), "Name already exists");
    } else if (!entity.getDeviceCommands().getDeviceCommands().isEmpty() ||
            !entity.getDeviceProperties().getDeviceProperties().isEmpty()) {

      DeviceClass deviceClass = deviceClassCache.get(entity.getClassId());
      configureChildElements(entity, deviceClass);
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
      throw createConfigurationException(entity.getName(), "No deviceClass with name " + entity.getClassName() + " exists.");
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

  /**
   * Each {@link DeviceProperty} must correspond to a {@link Property} defined in the Device's device class, meaning
   * that both name and ID must match. The ID is here fetched from the cache.
   * @param entity the device to be created
   * @param deviceClass the device class which the entity is created for
   */
  private void configureChildElements(Device entity, DeviceClass deviceClass) {
    for (DeviceProperty deviceProperty : entity.getDeviceProperties().getDeviceProperties()) {
      Property property = getDevClassElementAndConfigure(deviceProperty, deviceClass.getProperties(), entity.getName());

      if (deviceProperty.getFields() != null && property.getFields() == null && !deviceProperty.getFields().isEmpty()) {
        String fieldNames = String.join(", ", deviceProperty.getFields().keySet());
        throw createConfigurationException(entity.getName(), "DeviceFields \"" + fieldNames + "\" must refer to fields defined in parent class");
      } else if (deviceProperty.getFields() != null && property.getFields() != null) {
        for (DeviceProperty deviceField : deviceProperty.getFields().values()) {
          getDevClassElementAndConfigure(deviceField, property.getFields(), entity.getName());
        }
      }
    }

    for (DeviceCommand deviceCommand : entity.getDeviceCommands().getDeviceCommands()) {
      getDevClassElementAndConfigure(deviceCommand, deviceClass.getCommands(), entity.getName());
    }
  }

  private <T extends DeviceClassElement> T getDevClassElementAndConfigure(DeviceElement deviceElement, List<T> deviceClassElements, String deviceName) {
    Optional<T> devClassElement;
    if (deviceElement.getId() == null) {
      devClassElement = deviceClassElements
              .stream()
              .filter(p -> p.getName().equals(deviceElement.getName()))
              .findFirst();
      devClassElement.ifPresent(value -> deviceElement.setId(value.getId()));
    }
    devClassElement = deviceClassElements
            .stream()
            .filter(p -> p.getId().equals(deviceElement.getId()))
            .findFirst();
    if (devClassElement.isPresent() && elementsCorrespond(deviceElement, devClassElement.get())) {
      deviceElement.setName(devClassElement.get().getName());
      return devClassElement.get();
    } else {
      throw createConfigurationException(deviceName, deviceElement.getClass().getSimpleName() + "" +
              " \"" + deviceElement.getName() + "\" must refer to a corresponding element defined in parent class");
    }
  }

  private boolean elementsCorrespond(DeviceElement deviceElement, DeviceClassElement deviceClassElement) {
    return deviceClassElement.getName().equals(deviceElement.getName()) &&
            deviceClassElement.getId().equals(deviceElement.getId());
  }

  private ConfigurationParseException createConfigurationException(String deviceName, String cause) {
    return new ConfigurationParseException("Error creating device " + deviceName + ": " + cause);
  }
}