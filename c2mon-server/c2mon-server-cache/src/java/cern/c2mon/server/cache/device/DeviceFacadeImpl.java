/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.server.cache.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.DeviceCache;
import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.DeviceFacade;
import cern.c2mon.server.cache.common.AbstractFacade;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import cern.c2mon.server.common.device.DeviceCommandList;
import cern.c2mon.server.common.device.DevicePropertyList;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

/**
 * Implementation of the Device facade which defines the methods provided for
 * interacting with the {@link Device} cache.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class DeviceFacadeImpl extends AbstractFacade<Device> implements DeviceFacade {

  /**
   * Reference to the <code>Device</code> cache
   */
  private DeviceCache deviceCache;

  /**
   * Reference to the <code>DeviceClass</code> cache.
   */
  private DeviceClassCache deviceClassCache;

  /**
   * Default constructor used by Spring to autowire the device and device class
   * cache beans.
   *
   * @param pDeviceCache reference to the Device cache bean
   * @param pDeviceClassCache reference to the DeviceClass cache bean
   */
  @Autowired
  public DeviceFacadeImpl(final DeviceCache pDeviceCache, final DeviceClassCache pDeviceClassCache) {
    deviceCache = pDeviceCache;
    deviceClassCache = pDeviceClassCache;
  }

  @Override
  public List<Device> getDevices(String deviceClassName) {
    List<Device> devices = new ArrayList<>();

    // Search the name attribute of the class cache
    DeviceClassCacheObject deviceClass = (DeviceClassCacheObject) deviceClassCache.getDeviceClassByName(deviceClassName);
    List<Long> deviceIds = deviceClass.getDeviceIds();

    for (Long deviceId : deviceIds) {
      devices.add(deviceCache.getCopy(deviceId));
    }

    return devices;
  }

  @Override
  public Device createCacheObject(Long id, Properties properties) throws IllegalAccessException {
    DeviceCacheObject deviceCacheObject = new DeviceCacheObject(id);
    configureCacheObject(deviceCacheObject, properties);
    validateConfig(deviceCacheObject);
    return deviceCacheObject;
  }

  @Override
  protected Change configureCacheObject(Device cacheObject, Properties properties) throws IllegalAccessException {
    DeviceCacheObject deviceCacheObject = (DeviceCacheObject) cacheObject;

    if (properties.getProperty("name") != null) {
      deviceCacheObject.setName(properties.getProperty("name"));
    }
    if (properties.getProperty("classId") != null) {
      deviceCacheObject.setDeviceClassId(Long.parseLong(properties.getProperty("classId")));
    }

    // Parse properties and commands from XML representation
    if (properties.getProperty("deviceProperties") != null) {
      try {
        List<DeviceProperty> deviceProperties = parseDevicePropertiesXML(properties.getProperty("deviceProperties"));
        deviceCacheObject.setDeviceProperties(deviceProperties);
      } catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            "Exception: Unable to create device property list from parameter \"deviceProperties\": " + e + "\n" + properties.getProperty("deviceProperties"));
      }
    }

    if (properties.getProperty("deviceCommands") != null) {
      try {
        List<DeviceCommand> deviceCommands = parseDeviceCommandsXML(properties.getProperty("deviceCommands"));
        deviceCacheObject.setDeviceCommands(deviceCommands);
      } catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            "Exception: Unable to create device command list from parameter \"deviceCommands\":" + e + "\n" + properties.getProperty("deviceCommands"));
      }
    }

    return null;
  }

  @Override
  protected void validateConfig(Device cacheObject) {

    if (cacheObject.getId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
    }
    if (cacheObject.getName() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be null");
    }
    if (cacheObject.getName().length() == 0) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be empty");
    }
    if (cacheObject.getDeviceClassId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"deviceClassId\" cannot be null");
    }
    if (cacheObject.getDeviceProperties() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"deviceProperties\" cannot be null");
    }
    if (cacheObject.getDeviceCommands() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"deviceCommands\" cannot be null");
    }

    // Cross-check device class ID
    DeviceClass deviceClass = deviceClassCache.get(cacheObject.getDeviceClassId());
    if (deviceClass == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"deviceClassId\" must refer to an existing DeviceClass");
    }

    // Cross-check properties
    for (DeviceProperty deviceProperty : cacheObject.getDeviceProperties()) {
      if (!deviceClass.getPropertyNames().contains(deviceProperty.getName())) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "DeviceProperty \"" + deviceProperty.getName()
            + "\" (id: " + deviceProperty.getId() + ") must refer to a property defined in parent class");
      }

      if (!deviceClass.getPropertyIds().contains(deviceProperty.getId())) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "DeviceProperty \"" + deviceProperty.getName()
            + "\" specifies incorrect ID (does not match corresponding parent class property)");
      }

      // Cross-check fields
      if (deviceProperty.getFields() != null) {
        for (DeviceProperty field : deviceProperty.getFields().values()) {

          if (!deviceClass.getFieldNames(deviceProperty.getName()).contains(field.getName())) {
            throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "PropertyField \"" + field.getName()
                + "\" (id: " + field.getId() + ") must refer to a field defined in parent class property");
          }

          if (!deviceClass.getFieldIds(deviceProperty.getName()).contains(field.getId())) {
            throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "PropertyField \"" + field.getName()
                + "\" specifies incorrect ID (does not match corresponding parent property)");
          }
        }
      }

      // Sanity check on category
      if (deviceProperty.getCategory() == null && deviceProperty.getFields() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "DeviceProperty \"" + deviceProperty.getName()
            + "\" must specify a value category");
      }

      try {
        deviceProperty.getResultTypeClass();
      } catch (ClassNotFoundException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "DeviceProperty \"" + deviceProperty.getName()
            + "\" specifies invalid result type");
      }
    }

    // Cross-check commands
    for (DeviceCommand deviceCommand : cacheObject.getDeviceCommands()) {
      if (!deviceClass.getCommandNames().contains(deviceCommand.getName())) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "DeviceCommand \"" + deviceCommand.getName()
            + "\" must refer to a command defined in parent device class");
      }

      if (!deviceClass.getCommandIds().contains(deviceCommand.getId())) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "DeviceCommand \"" + deviceCommand.getName()
            + "\" specifies incorrect ID (does not match corresponding parent class command)");
      }
    }
  }

  /**
   * Parse the XML representation of the properties of a device (which comes
   * from configuration) and return it as a list of {@link DeviceProperty}
   * objects.
   *
   * @param xmlString the XML representation string of the device properties
   *
   * @return the list of device properties
   * @throws Exception if the XML could not be parsed
   */
  private List<DeviceProperty> parseDevicePropertiesXML(String xmlString) throws Exception {
    List<DeviceProperty> deviceProperties = new ArrayList<>();

    Serializer serializer = new Persister();
    DevicePropertyList devicePropertyList = serializer.read(DevicePropertyList.class, xmlString);

    for (DeviceProperty deviceProperty : devicePropertyList.getDeviceProperties()) {
      deviceProperties.add(deviceProperty);
    }

    return deviceProperties;
  }

  /**
   * Parse the XML representation of the commands of a device (which comes from
   * configuration) and return it as a list of {@link DeviceCommand} objects.
   *
   * @param xmlString the XML representation string of the device commands
   *
   * @return the list of device commands
   * @throws Exception if the XML could not be parsed
   */
  private List<DeviceCommand> parseDeviceCommandsXML(String xmlString) throws Exception {
    List<DeviceCommand> deviceCommands = new ArrayList<>();

    Serializer serializer = new Persister();
    DeviceCommandList deviceCommandList = serializer.read(DeviceCommandList.class, xmlString);

    for (DeviceCommand deviceCommand : deviceCommandList.getDeviceCommands()) {
      deviceCommands.add(deviceCommand);
    }

    return deviceCommands;
  }

}
