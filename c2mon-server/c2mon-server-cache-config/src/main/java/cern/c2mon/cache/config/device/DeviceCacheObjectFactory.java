package cern.c2mon.cache.config.device;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.common.device.*;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.common.validation.MicroValidator;
import cern.c2mon.shared.daq.config.Change;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Properties;

import static cern.c2mon.cache.api.parser.XmlParser.parse;

/**
 * @author Szymon Halastra
 */
@Component
public class DeviceCacheObjectFactory extends AbstractCacheObjectFactory<Device> {

  private C2monCache<DeviceClass> deviceClassCacheRef;

  @Inject
  public DeviceCacheObjectFactory(C2monCache<DeviceClass> deviceClassCacheRef) {
    this.deviceClassCacheRef = deviceClassCacheRef;
  }

  @Override
  public Device createCacheObject(Long id) {
    return new DeviceCacheObject(id, null, null);
  }

  @Override
  public Change configureCacheObject(Device device, Properties properties) {
    DeviceCacheObject deviceCacheObject = (DeviceCacheObject) device;

    new PropertiesAccessor(properties)
      .getString("name").ifPresent(deviceCacheObject::setName)
      .getLong("classId").ifPresent(deviceCacheObject::setDeviceClassId)
      .getAs("deviceProperties", prop -> parse(prop, DevicePropertyList.class))
        .ifPresent(deviceCacheObject::setDeviceProperties)
      .getAs("deviceCommands", prop -> parse(prop, DeviceCommandList.class))
        .ifPresent(deviceCacheObject::setDeviceCommands);

    return null;
  }

  @Override
  public void validateConfig(Device device) throws ConfigurationException {
    new MicroValidator<>(device)
      .notNull(Device::getId, "id")
      .notNull(Device::getName, "name")
      .not(deviceObj -> deviceObj.getName().isEmpty(), "Parameter \"name\" cannot be empty")
      .notNull(Device::getDeviceClassId, "deviceClassId")
      .notNull(Device::getDeviceProperties, "deviceProperties")
      .must(deviceObj -> deviceClassCacheRef.containsKey(deviceObj.getDeviceClassId()), "Parameter \"deviceClassId\" must refer to an existing DeviceClass")
      .notNull(Device::getDeviceCommands, "deviceCommands");

    // Cross-check device class ID
    DeviceClass deviceClass = deviceClassCacheRef.get(device.getDeviceClassId());

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
}
