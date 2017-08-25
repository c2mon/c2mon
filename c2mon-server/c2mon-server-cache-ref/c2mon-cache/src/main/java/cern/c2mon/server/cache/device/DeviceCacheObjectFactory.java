package cern.c2mon.server.cache.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.CacheObjectFactory;
import cern.c2mon.server.common.device.*;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

/**
 * @author Szymon Halastra
 */
@Component
public class DeviceCacheObjectFactory extends CacheObjectFactory<Device> {

  private C2monCache<Long, DeviceClass> deviceClassCache;

  @Autowired
  public DeviceCacheObjectFactory(C2monCache<Long, DeviceClass> deviceClassCache) {
    this.deviceClassCache = deviceClassCache;
  }

  @Override
  public Change configureCacheObject(Device device, Properties properties) {
    DeviceCacheObject deviceCacheObject = (DeviceCacheObject) device;

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
      }
      catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
                "Exception: Unable to create device property list from parameter \"deviceProperties\": " + e + "\n" + properties.getProperty("deviceProperties"));
      }
    }

    if (properties.getProperty("deviceCommands") != null) {
      try {
        List<DeviceCommand> deviceCommands = parseDeviceCommandsXML(properties.getProperty("deviceCommands"));
        deviceCacheObject.setDeviceCommands(deviceCommands);
      }
      catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
                "Exception: Unable to create device command list from parameter \"deviceCommands\":" + e + "\n" + properties.getProperty("deviceCommands"));
      }
    }

    return null;
  }

  @Override
  public void validateConfig(Device device) throws ConfigurationException {
    if (device.getId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
    }
    if (device.getName() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be null");
    }
    if (device.getName().length() == 0) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be empty");
    }
    if (device.getDeviceClassId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"deviceClassId\" cannot be null");
    }
    if (device.getDeviceProperties() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"deviceProperties\" cannot be null");
    }
    if (device.getDeviceCommands() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"deviceCommands\" cannot be null");
    }

    // Cross-check device class ID
    DeviceClass deviceClass = deviceClassCache.get(device.getDeviceClassId());
    if (deviceClass == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"deviceClassId\" must refer to an existing DeviceClass");
    }

    // Cross-check properties
    for (DeviceProperty deviceProperty : device.getDeviceProperties()) {
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
      }
      catch (ClassNotFoundException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "DeviceProperty \"" + deviceProperty.getName()
                + "\" specifies invalid result type");
      }
    }

    // Cross-check commands
    for (DeviceCommand deviceCommand : device.getDeviceCommands()) {
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
