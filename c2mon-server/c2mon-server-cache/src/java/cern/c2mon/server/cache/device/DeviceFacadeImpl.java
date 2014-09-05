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
import cern.c2mon.server.common.device.CommandValueList;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import cern.c2mon.server.common.device.PropertyValueList;
import cern.c2mon.shared.client.device.CommandValue;
import cern.c2mon.shared.client.device.PropertyValue;
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

    // Parse property and command values from XML representation
    if (properties.getProperty("propertyValues") != null) {
      try {
        List<PropertyValue> propertyValues = parseXmlPropertyValues(properties.getProperty("propertyValues"));
        deviceCacheObject.setPropertyValues(propertyValues);
      } catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            "Exception: Unable to create property value list from parameter \"propertyValues\": " + e + "\n" + properties.getProperty("propertyValues"));
      }
    }

    if (properties.getProperty("commandValues") != null) {
      try {
        List<CommandValue> commandValues = parseXmlCommandValues(properties.getProperty("commandValues"));
        deviceCacheObject.setCommandValues(commandValues);
      } catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            "Exception: Unable to create command value list from parameter \"commands\":" + e + "\n" + properties.getProperty("commands"));
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
    if (cacheObject.getPropertyValues() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"propertyValues\" cannot be null");
    }
    if (cacheObject.getCommandValues() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"commandValues\" cannot be null");
    }

    // Cross-check device class ID
    DeviceClass deviceClass = deviceClassCache.get(cacheObject.getDeviceClassId());
    if (deviceClass == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"deviceClassId\" must refer to an existing DeviceClass");
    }

    // Cross-check properties and commands
    for (PropertyValue propertyValue : cacheObject.getPropertyValues()) {
      if (!deviceClass.getProperties().contains(propertyValue.getName())) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Property \"" + propertyValue.getName()
            + "\" must refer to an existing DeviceClass property");
      }

      // Sanity check on (tagId / clientRule / constantValue / resultType)
      if (propertyValue.getTagId() == null && propertyValue.getClientRule() == null && propertyValue.getConstantValue() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Property \"" + propertyValue.getName()
            + "\" must specify at least one of (tagId, clientRule, constantValue)");
      }

      try {
        propertyValue.getResultType();
      } catch (ClassNotFoundException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Property \"" + propertyValue.getName()
            + "\" specifies invalid result type");
      }
    }

    for (CommandValue commandValue : cacheObject.getCommandValues()) {
      if (!deviceClass.getCommands().contains(commandValue.getName())) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Command \"" + commandValue.getName()
            + "\" must refer to an existing DeviceClass command");
      }
    }
  }

  /**
   * Parse the XML representation of the property values of a device (which
   * comes from configuration) and return it as a list of {@link PropertyValue}
   * objects.
   *
   * @param xmlString the XML representation string of the device property
   *          values
   *
   * @return the list of property values
   * @throws Exception if the XML could not be parsed
   */
  private List<PropertyValue> parseXmlPropertyValues(String xmlString) throws Exception {
    List<PropertyValue> propertyValues = new ArrayList<>();

    Serializer serializer = new Persister();
    PropertyValueList propertyValueList = serializer.read(PropertyValueList.class, xmlString);

    for (PropertyValue propertyValue : propertyValueList.getPropertyValues()) {
      propertyValues.add(propertyValue);
    }

    return propertyValues;
  }

  /**
   * Parse the XML representation of the command values of a device (which comes
   * from configuration) and return it as a list of {@link CommandValue}
   * objects.
   *
   * @param xmlString the XML representation string of the device command values
   *
   * @return the list of command values
   * @throws Exception if the XML could not be parsed
   */
  private List<CommandValue> parseXmlCommandValues(String xmlString) throws Exception {
    List<CommandValue> commandValues = new ArrayList<>();

    Serializer serializer = new Persister();
    CommandValueList commandValueList = serializer.read(CommandValueList.class, xmlString);

    for (CommandValue commandValue : commandValueList.getCommandValues()) {
      commandValues.add(commandValue);
    }

    return commandValues;
  }

}
