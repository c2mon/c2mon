package cern.c2mon.server.cache;

import java.util.Properties;

import cern.c2mon.cache.api.factory.CacheObjectFactory;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.common.equipment.AbstractEquipmentCacheObject;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.EquipmentConfigurationUpdate;

/**
 * @author Szymon Halastra
 */
public abstract class AbstractEquipmentCacheObjectFactory<T extends AbstractEquipment> extends CacheObjectFactory<T> {

  @Override
  public void validateConfig(T abstractEquipment) throws ConfigurationException {
    AbstractEquipmentCacheObject abstractEquipmentCacheObject = (AbstractEquipmentCacheObject) abstractEquipment;
    if (abstractEquipmentCacheObject.getId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
    }
    if (abstractEquipmentCacheObject.getName() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be null");
    }
    if (abstractEquipmentCacheObject.getName().length() == 0 || abstractEquipmentCacheObject.getName().length() > 60) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" must be 1 to 60 characters long");
    }
    if (abstractEquipmentCacheObject.getDescription() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" cannot be null");
    }
    if (abstractEquipmentCacheObject.getDescription().length() == 0 || abstractEquipmentCacheObject.getDescription().length() > 100) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" must be 1 to 100 characters long");
    }
    if (abstractEquipmentCacheObject.getStateTagId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"stateTagId\" cannot be null. Each equipment MUST have a registered state tag.");
    }
    if (abstractEquipmentCacheObject.getCommFaultTagId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"commFaultTagId\" cannot be null. Each equipment MUST have a registered communication fault tag.");
    }
    if (abstractEquipmentCacheObject.getAliveInterval() < 10000) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"aliveInterval\" must be >= 10000 milliseconds. It makes no sense to send the alive tag too often.");
    }
  }

  /**
   * Sets the common properties for Equipment and Subequipment cache objects
   * when creating or updating them.
   *
   * @param abstractEquipmentCacheObject
   * @param properties
   */
  protected EquipmentConfigurationUpdate setCommonProperties(T abstractEquipment, Properties properties) {
    AbstractEquipmentCacheObject abstractEquipmentCacheObject = (AbstractEquipmentCacheObject) abstractEquipment;
    EquipmentConfigurationUpdate configurationUpdate = new EquipmentConfigurationUpdate();
    configurationUpdate.setEquipmentId(abstractEquipment.getId());
    String tmpStr = null;

    // Set the process name and all parameters DERIVED from the process name
    tmpStr = properties.getProperty("name");
    if (tmpStr != null) {
      abstractEquipmentCacheObject.setName(tmpStr);
      configurationUpdate.setName(tmpStr);
    }

    if (properties.getProperty("description") != null)
      abstractEquipmentCacheObject.setDescription(properties.getProperty("description"));

    if ((tmpStr = properties.getProperty("aliveTagId")) != null) {
      try {
        Long aliveId = Long.valueOf(tmpStr);
        abstractEquipmentCacheObject.setAliveTagId(aliveId);
        configurationUpdate.setAliveTagId(aliveId);
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"aliveTagId\" to Long: " + tmpStr);
      }
    }

    if ((tmpStr = properties.getProperty("aliveInterval")) != null) {
      try {
        abstractEquipmentCacheObject.setAliveInterval(Integer.parseInt(tmpStr));
        configurationUpdate.setAliveInterval(Long.parseLong(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"aliveInterval\" to Integer: " + tmpStr);
      }
    }

    if ((tmpStr = properties.getProperty("stateTagId")) != null || (tmpStr = properties.getProperty("statusTagId")) != null) {
      try {
        abstractEquipmentCacheObject.setStateTagId(Long.valueOf(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"stateTagId\" to Long: " + tmpStr);
      }
    }

    if (properties.getProperty("handlerClass") != null) {
      abstractEquipmentCacheObject.setHandlerClassName(properties.getProperty("handlerClass"));
    }

    if ((tmpStr = properties.getProperty("commFaultTagId")) != null) {
      try {
        Long commfaultTagId = Long.valueOf(tmpStr);
        abstractEquipmentCacheObject.setCommFaultTagId(commfaultTagId);
        configurationUpdate.setCommfaultTagId(commfaultTagId);
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"commFaultTagId\" to Long: " + tmpStr);
      }
    }
    return configurationUpdate;
  }
}
