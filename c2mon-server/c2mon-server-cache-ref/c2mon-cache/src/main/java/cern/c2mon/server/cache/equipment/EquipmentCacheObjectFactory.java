package cern.c2mon.server.cache.equipment;

import java.util.Properties;

import org.springframework.stereotype.Component;

import cern.c2mon.server.cache.AbstractEquipmentCacheObjectFactory;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.EquipmentConfigurationUpdate;

/**
 * @author Szymon Halastra
 */
@Component
public class EquipmentCacheObjectFactory extends AbstractEquipmentCacheObjectFactory<Equipment> {

  public EquipmentCacheObjectFactory() {
  }

  @Override
  public Equipment createCacheObject(Long id) {
    return new EquipmentCacheObject(id);
  }

  /**
   * Overridden as for Equipment rule out updating the process this equipment
   * is associated to.
   *
   * @throws IllegalAccessException
   */
  @Override
  public Change updateConfig(Equipment equipment, Properties properties) throws IllegalAccessException {
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
    //only validated for Equipment, although also set in SubEquipment (but not used there - only DB related and inherited from TIM1! -> TODO remove handler class name form subequipment cache object and adapt loading SQL)
    if (equipmentCacheObject.getHandlerClassName() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"handlerClassName\" cannot be null");
    }

    if (equipmentCacheObject.getProcessId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"processId\" cannot be null. An equipment MUST be attached to a process.");
    }
  }

  @Override
  public Change configureCacheObject(Equipment equipment, Properties properties) throws IllegalAccessException {
    EquipmentCacheObject equipmentCacheObject = (EquipmentCacheObject) equipment;
    EquipmentConfigurationUpdate configurationUpdate = setCommonProperties(equipmentCacheObject, properties);
    String tmpStr = properties.getProperty("address");
    if (tmpStr != null) {
      equipmentCacheObject.setAddress(tmpStr);
      configurationUpdate.setEquipmentAddress(tmpStr);
    }

    //never set when called from config update method
    if ((tmpStr = properties.getProperty("processId")) != null) {
      try {
        equipmentCacheObject.setProcessId(Long.valueOf(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"processId\" to Long: " + tmpStr);
      }
    }
    return configurationUpdate;
  }
}
