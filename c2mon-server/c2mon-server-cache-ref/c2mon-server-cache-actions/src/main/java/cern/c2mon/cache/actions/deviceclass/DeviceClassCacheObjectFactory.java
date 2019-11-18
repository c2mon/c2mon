package cern.c2mon.cache.actions.deviceclass;

import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.common.device.CommandList;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import cern.c2mon.server.common.device.PropertyList;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.common.validation.MicroValidator;
import cern.c2mon.shared.daq.config.Change;
import org.springframework.stereotype.Component;

import java.util.Properties;

import static cern.c2mon.cache.api.parser.XmlParser.parse;

/**
 * @author Szymon Halastra
 */
@Component
public class DeviceClassCacheObjectFactory extends AbstractCacheObjectFactory<DeviceClass> {

  @Override
  public DeviceClass createCacheObject(Long id) {
    return new DeviceClassCacheObject(id);
  }

  @Override
  public Change configureCacheObject(DeviceClass deviceClass, Properties properties) {
    DeviceClassCacheObject deviceClassCacheObject = (DeviceClassCacheObject) deviceClass;

    new PropertiesAccessor(properties)
      .getString("name").ifPresent(deviceClassCacheObject::setName)
      .getString("description").ifPresent(deviceClassCacheObject::setDescription)
      .getAs("properties", prop -> parse(prop, PropertyList.class))
      .ifPresent(deviceClassCacheObject::setProperties)
      .getAs("commands", prop -> parse(prop, CommandList.class))
      .ifPresent(deviceClassCacheObject::setCommands);

    return null;
  }

  @Override
  public void validateConfig(DeviceClass deviceClass) throws ConfigurationException {
    new MicroValidator<>(deviceClass)
      .notNull(DeviceClass::getId, "id")
      .notNull(DeviceClass::getName, "name")
      .not(deviceObj -> deviceObj.getName().isEmpty(), "Parameter \"name\" cannot be empty")
      .notNull(DeviceClass::getProperties, "properties")
      .notNull(DeviceClass::getCommands, "commands");
  }
}
