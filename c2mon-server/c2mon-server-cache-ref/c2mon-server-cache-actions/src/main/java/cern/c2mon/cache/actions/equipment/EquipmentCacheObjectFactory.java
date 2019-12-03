package cern.c2mon.cache.actions.equipment;

import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.common.validation.MicroValidator;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.EquipmentConfigurationUpdate;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou
 */
@Component
public class EquipmentCacheObjectFactory extends AbstractEquipmentCacheObjectFactory<Equipment> {

  @Override
  public Equipment createCacheObject(Long id) {
    return new EquipmentCacheObject(id);
  }

  @Override
  public Change updateConfig(Equipment equipment, Properties properties) {
    if ((properties.getProperty("processId")) != null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Reconfiguration of "
              + "Equipment does not currently allow it to be reassigned to a different Process!");
    }
    return super.updateConfig(equipment, properties);
  }

  @Override
  public void validateConfig(Equipment equipment) throws ConfigurationException {
    EquipmentCacheObject equipmentCacheObject = (EquipmentCacheObject) equipment;
    super.validateConfig(equipmentCacheObject);

    new MicroValidator<>(equipmentCacheObject)
      .notNull(EquipmentCacheObject::getHandlerClassName, "handlerClassName")
      .notNull(Equipment::getProcessId, "processId");
  }

  @Override
  public Change configureCacheObject(Equipment equipment, Properties properties) {
    EquipmentCacheObject equipmentCacheObject = (EquipmentCacheObject) equipment;
    EquipmentConfigurationUpdate configurationUpdate = setCommonProperties(equipmentCacheObject, properties);

    new PropertiesAccessor(properties)
      .getString("address").ifPresent(address -> {
      equipmentCacheObject.setAddress(address);
      configurationUpdate.setEquipmentAddress(address);
    }).getLong("processId").ifPresent(equipmentCacheObject::setProcessId);

    return configurationUpdate;
  }
}
