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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import cern.c2mon.server.common.device.CommandValueList.CommandValue;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import cern.c2mon.server.common.device.PropertyValueList;
import cern.c2mon.server.common.device.PropertyValueList.PropertyValue;
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
        Map<String, Long> propertyValues = parseXmlPropertyValues(properties.getProperty("propertyValues"));
        deviceCacheObject.setPropertyValues(propertyValues);
      } catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            "Exception: Unable to create property value list from parameter \"propertyValues\": \n" + properties.getProperty("propertyValues"));
      }
    }

    if (properties.getProperty("commandValues") != null) {
      try {
        Map<String, Long> commandValues = parseXmlCommandValues(properties.getProperty("commandValues"));
        deviceCacheObject.setCommandValues(commandValues);
      } catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            "Exception: Unable to create command value list from parameter \"commands\": \n" + properties.getProperty("commands"));
      }
    }

    return null;
  }

  @Override
  protected void validateConfig(Device cacheObject) {
    try {
      deviceCache.acquireReadLockOnKey(cacheObject.getId());

      if (cacheObject.getId() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
      }
      if (cacheObject.getName() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be null");
      }
      if (cacheObject.getName().length() == 0 ) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be empty");
      }
      if (cacheObject.getPropertyValues() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"propertyValues\" cannot be null");
      }
      if (cacheObject.getCommandValues() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"commandValues\" cannot be null");
      }

    } finally {
      deviceCache.releaseReadLockOnKey(cacheObject.getId());
    }
  }

  /**
   * Parse the XML representation of the property values of a device (which
   * comes from configuration) and return it as a map of (property names : tag
   * IDs).
   *
   * @param xmlString the XML representation string of the device property
   *          values
   *
   * @return the map of (property names : tag IDs)
   * @throws Exception if the XML could not be parsed
   */
  private Map<String, Long> parseXmlPropertyValues(String xmlString) throws Exception {
    Map<String, Long> propertyValues = new HashMap<>();

    Serializer serializer = new Persister();
    PropertyValueList propertyValueList = serializer.read(PropertyValueList.class, xmlString);

    for (PropertyValue propertyValue : propertyValueList.getPropertyValues()) {
      propertyValues.put(propertyValue.getName(), propertyValue.getTagId());
    }

    return propertyValues;
  }

  /**
   * Parse the XML representation of the command values of a device (which comes
   * from configuration) and return it as a map of (command names : tag IDs).
   *
   * @param xmlString the XML representation string of the device command values
   *
   * @return the map of (command names : tag IDs)
   * @throws Exception if the XML could not be parsed
   */
  private Map<String, Long> parseXmlCommandValues(String xmlString) throws Exception {
    Map<String, Long> commandValues = new HashMap<>();

    Serializer serializer = new Persister();
    CommandValueList commandValueList = serializer.read(CommandValueList.class, xmlString);

    for (CommandValue commandValue : commandValueList.getCommandValues()) {
      commandValues.put(commandValue.getName(), commandValue.getTagId());
    }

    return commandValues;
  }

}
