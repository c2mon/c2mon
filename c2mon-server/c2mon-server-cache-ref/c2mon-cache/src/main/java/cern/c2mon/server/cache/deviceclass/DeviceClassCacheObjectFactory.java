package cern.c2mon.server.cache.deviceclass;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import cern.c2mon.cache.api.factory.CacheObjectFactory;
import cern.c2mon.server.common.device.*;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

/**
 * @author Szymon Halastra
 */
public class DeviceClassCacheObjectFactory extends CacheObjectFactory<DeviceClass> {

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

    return null;  }

  @Override
  public void validateConfig(DeviceClass deviceClass) throws ConfigurationException {
      if (deviceClass.getId() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
      }
      if (deviceClass.getName() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be null");
      }
      if (deviceClass.getName().length() == 0 ) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be empty");
      }
      if (deviceClass.getProperties() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"properties\" cannot be null");
      }
      if (deviceClass.getCommands() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"commands\" cannot be null");
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
