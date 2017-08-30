package cern.c2mon.server.cache.deviceclass.components;

import java.util.List;
import java.util.Properties;

import org.springframework.stereotype.Component;

import cern.c2mon.cache.api.factory.CacheObjectFactory;
import cern.c2mon.cache.api.parser.XmlParser;
import cern.c2mon.server.common.device.*;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

/**
 * @author Szymon Halastra
 */
@Component
public class DeviceClassCacheObjectFactory extends CacheObjectFactory<DeviceClass> {

  @Override
  public DeviceClass createCacheObject(Long id) {
    DeviceClassCacheObject deviceClassCacheObject = new DeviceClassCacheObject(id);

    return deviceClassCacheObject;
  }

  @Override
  public Change configureCacheObject(DeviceClass deviceClass, Properties properties) {
    DeviceClassCacheObject deviceClassCacheObject = (DeviceClassCacheObject) deviceClass;

    if (properties.getProperty("name") != null) {
      deviceClassCacheObject.setName(properties.getProperty("name"));
    }
    if (properties.getProperty("description") != null) {
      deviceClassCacheObject.setDescription(properties.getProperty("description"));
    }

    // Parse properties and commands from XML representation
    if (properties.getProperty("properties") != null) {
      List<Property> propertyNames = XmlParser.parseXmlProperties(properties.getProperty("properties"), PropertyList.class);
      deviceClassCacheObject.setProperties(propertyNames);
    }

    if (properties.getProperty("commands") != null) {
      List<Command> commandNames = XmlParser.parseXmlCommands(properties.getProperty("commands"), CommandList.class);
      deviceClassCacheObject.setCommands(commandNames);
    }

    return null;
  }

  @Override
  public void validateConfig(DeviceClass deviceClass) throws ConfigurationException {
    if (deviceClass.getId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
    }
    if (deviceClass.getName() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be null");
    }
    if (deviceClass.getName().length() == 0) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be empty");
    }
    if (deviceClass.getProperties() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"properties\" cannot be null");
    }
    if (deviceClass.getCommands() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"commands\" cannot be null");
    }
  }
}
