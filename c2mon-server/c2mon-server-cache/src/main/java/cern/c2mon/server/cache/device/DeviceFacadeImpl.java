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
package cern.c2mon.server.cache.device;

import java.util.*;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.DeviceCache;
import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.DeviceFacade;
import cern.c2mon.server.cache.common.AbstractFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.device.*;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceInfo;
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
   * Static class logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DeviceCacheImpl.class);

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

    try {
      // Search the name attribute of the class cache
      long deviceClassId = deviceClassCache.getDeviceClassIdByName(deviceClassName);

      devices = deviceCache.getByDeviceClassId(deviceClassId);

    } catch (CacheElementNotFoundException e) {
      // If we didn't find a class with the given name, return an empty list.
      LOG.warn("Error getting device class by name", e);
      return devices;
    }

    return devices;
  }

  @Override
  public List<Device> getDevices(Set<DeviceInfo> deviceInfoList) {
    List<Device> devices = new ArrayList<>();

    // Reorganise the data structure to make processing a bit easier
    Map<String, Set<String>> classNamesToDeviceNames = new HashMap<>();
    for (DeviceInfo deviceInfo : deviceInfoList) {
      if (!classNamesToDeviceNames.containsKey(deviceInfo.getClassName())) {
        classNamesToDeviceNames.put(deviceInfo.getClassName(), new HashSet<>(Arrays.asList(deviceInfo.getDeviceName())));
      } else {
        classNamesToDeviceNames.get(deviceInfo.getClassName()).add(deviceInfo.getDeviceName());
      }
    }

    // Build up a list of requested devices. Note that this list may not be
    // complete. It is the client's responsibility to check the completeness of
    // the returned list.
    for (Map.Entry<String, Set<String>> entry : classNamesToDeviceNames.entrySet()) {
      String className = entry.getKey();
      Set<String> deviceNames = entry.getValue();

      try {
        List<Device> deviceList = getDevices(className);

        for (Device device : deviceList) {
          if (deviceNames.contains(device.getName())) {
            devices.add(device);
          }
        }

      } catch (CacheElementNotFoundException e) {
        LOG.warn("Didn't find any devices of class " + className, e);
      }
    }

    return devices;
  }

  @Override
  public String getClassNameForDevice(Long deviceId) {
    return deviceClassCache.get(deviceCache.get(deviceId).getDeviceClassId()).getName();
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
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "DeviceProperty \"" + deviceProperty.getName() + "\" (id: "
            + deviceProperty.getId() + ") must refer to a property defined in parent class");
      }

      if (!deviceClass.getPropertyIds().contains(deviceProperty.getId())) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "DeviceProperty \"" + deviceProperty.getName()
            + "\" specifies incorrect ID (does not match corresponding parent class property)");
      }

      // Cross-check fields
      if (deviceProperty.getFields() != null) {
        for (DeviceProperty field : deviceProperty.getFields().values()) {

          if (!deviceClass.getFieldNames(deviceProperty.getName()).contains(field.getName())) {
            throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "PropertyField \"" + field.getName() + "\" (id: " + field.getId()
                + ") must refer to a field defined in parent class property");
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

      // Remove all whitespace and control characters
      if (deviceProperty.getValue() != null) {
        deviceProperty.setValue(deviceProperty.getValue().replaceAll("[\u0000-\u001f]", "").trim());
      }

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
