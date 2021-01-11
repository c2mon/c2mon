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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.DeviceClassFacade;
import cern.c2mon.server.cache.common.AbstractFacade;
import cern.c2mon.shared.client.device.Command;
import cern.c2mon.shared.client.device.CommandList;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import cern.c2mon.shared.client.device.Property;
import cern.c2mon.shared.client.device.PropertyList;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

/**
 * Implementation of the DeviceClass facade which defines the methods provided
 * for interacting with the {@link DeviceClass} cache.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class DeviceClassFacadeImpl extends AbstractFacade<DeviceClass> implements DeviceClassFacade {

  /**
   * Reference to the <code>DeviceClass</code> cache.
   */
  private DeviceClassCache deviceClassCache;

  /**
   * Default constructor used by Spring to autowire the device and device class
   * cache beans.
   *
   * @param pDeviceClassCache reference to the DeviceClass cache bean
   */
  @Autowired
  public DeviceClassFacadeImpl(final DeviceClassCache pDeviceClassCache) {
    deviceClassCache = pDeviceClassCache;
  }

  @Override
  public List<String> getDeviceClassNames() {
    List<String> classNames = new ArrayList<>();

    for (Long deviceClassId : deviceClassCache.getKeys()) {
      DeviceClass deviceClass = deviceClassCache.get(deviceClassId);
      classNames.add(deviceClass.getName());
    }

    return classNames;
  }

  @Override
  public DeviceClass createCacheObject(Long id, Properties properties) throws IllegalAccessException {
    DeviceClassCacheObject deviceClassCacheObject = new DeviceClassCacheObject(id);
    configureCacheObject(deviceClassCacheObject, properties);
    validateConfig(deviceClassCacheObject);
    return deviceClassCacheObject;
  }

  @Override
  protected Change configureCacheObject(DeviceClass cacheObject, Properties properties) throws IllegalAccessException {
    DeviceClassCacheObject deviceClassCacheObject = (DeviceClassCacheObject) cacheObject;

    if (properties.getProperty("name") != null) {
      deviceClassCacheObject.setName(properties.getProperty("name"));
    }
    if (properties.getProperty("description") != null) {
      deviceClassCacheObject.setDescription(properties.getProperty("description"));
    }

    // Parse properties and commands from XML representation
    if (properties.getProperty("properties") != null) {
      try {
        List<Property> propertyNames = parseXmlProperties(properties.getProperty("properties"));
        deviceClassCacheObject.setProperties(propertyNames);
      } catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            "Exception: Unable to create property list from parameter \"properties\": " + e + ":\n" + properties.getProperty("properties"));
      }
    }

    if (properties.getProperty("commands") != null) {
      try {
        List<Command> commandNames = parseXmlCommands(properties.getProperty("commands"));
        deviceClassCacheObject.setCommands(commandNames);
      } catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            "Exception: Unable to create command list from parameter \"commands\": " + e + ":\n" + properties.getProperty("commands"));
      }
    }

    return null;
  }

  @Override
  protected void validateConfig(DeviceClass cacheObject) {
    try {
      deviceClassCache.acquireReadLockOnKey(cacheObject.getId());

      if (cacheObject.getId() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
      }
      if (cacheObject.getName() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be null");
      }
      if (cacheObject.getName().length() == 0 ) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be empty");
      }
      if (cacheObject.getProperties() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"properties\" cannot be null");
      }
      if (cacheObject.getCommands() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"commands\" cannot be null");
      }

    } finally {
      deviceClassCache.releaseReadLockOnKey(cacheObject.getId());
    }
  }

  /**
   * Parse the XML representation of the properties of a device class (which
   * comes from configuration) and return it as a list of properties.
   *
   * @param xmlString the XML representation string of the device class
   *          properties
   *
   * @return the list of properties
   * @throws Exception if the XML could not be parsed
   */
  private List<Property> parseXmlProperties(String xmlString) throws Exception {
    List<Property> properties = new ArrayList<>();

    Serializer serializer = new Persister();
    PropertyList propertyList = serializer.read(PropertyList.class, xmlString);

    for (Property property : propertyList.getProperties()) {
      properties.add(property);
    }

    return properties;
  }

  /**
   * Parse the XML representation of the commands of a device class (which comes
   * from configuration) and return it as a list of commands.
   *
   * @param xmlString the XML representation string of the device class commands
   *
   * @return the list of commands
   * @throws Exception if the XML could not be parsed
   */
  private List<Command> parseXmlCommands(String xmlString) throws Exception {
    List<Command> commands = new ArrayList<>();

    Serializer serializer = new Persister();
    CommandList commandList = serializer.read(CommandList.class, xmlString);

    for (Command command : commandList.getCommands()) {
      commands.add(command);
    }

    return commands;
  }
}
