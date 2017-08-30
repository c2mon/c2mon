package cern.c2mon.server.cache.command.components;

import java.lang.reflect.Field;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.CacheObjectFactory;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.SimpleTypeReflectionHandler;
import cern.c2mon.shared.common.command.CommandTag;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.CommandTagUpdate;
import cern.c2mon.shared.daq.config.HardwareAddressUpdate;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Component
public class CommandCacheObjectFactory extends CacheObjectFactory<CommandTag> {

  private final C2monCache<Long, Equipment> equipmentCache;

  @Autowired
  public CommandCacheObjectFactory(C2monCache<Long, Equipment> equipmentCache) {
    this.equipmentCache = equipmentCache;
  }

  @Override
  public CommandTag createCacheObject(Long id) {
    CommandTagCacheObject commandTagCacheObject = new CommandTagCacheObject(id);

    return commandTagCacheObject;
  }

  @Override
  public Change configureCacheObject(CommandTag commandTag, Properties properties) {
    String tmpStr;
    CommandTagCacheObject commandTagCacheObject = (CommandTagCacheObject) commandTag;
    CommandTagUpdate commandTagUpdate = new CommandTagUpdate();
    commandTagUpdate.setCommandTagId(commandTag.getId());

    // name (String)
    if ((tmpStr = properties.getProperty("name")) != null) {
      commandTagCacheObject.setName(tmpStr);
      commandTagUpdate.setName(tmpStr);
    }
    // description (String)
    tmpStr = properties.getProperty("description");
    if (tmpStr != null) {
      commandTagCacheObject.setDescription(tmpStr);
    }
    // mode (short)
    if ((tmpStr = properties.getProperty("mode")) != null) {
      commandTagCacheObject.setMode(parseShort(tmpStr, "mode"));
    }
    // dataType (String)
    if (properties.getProperty("dataType") != null) {
      commandTagCacheObject.setDataType(properties.getProperty("dataType"));
    }

    // sourceRetries (int)
    if ((tmpStr = properties.getProperty("sourceRetries")) != null) {
      commandTagCacheObject.setSourceRetries(parseInt(tmpStr, "sourceRetries"));
      commandTagUpdate.setSourceRetries(parseInt(tmpStr, "sourceRetries"));
    }
    // sourceTimeout (int)
    if ((tmpStr = properties.getProperty("sourceTimeout")) != null) {
      commandTagCacheObject.setSourceTimeout(parseInt(tmpStr, "sourceTimeout"));
      commandTagUpdate.setSourceTimeout(parseInt(tmpStr, "sourceTimeout"));
    }
    // execTimeout (int)
    if ((tmpStr = properties.getProperty("execTimeout")) != null) {
      commandTagCacheObject.setExecTimeout(parseInt(tmpStr, "execTimeout"));
    }
    // clientTimeout (int)
    if ((tmpStr = properties.getProperty("clientTimeout")) != null) {
      commandTagCacheObject.setClientTimeout(parseInt(tmpStr, "clientTimeout"));
    }
    // hardwareAddress (String -> HardwareAddress)
    if ((tmpStr = properties.getProperty("hardwareAddress")) != null) {
      try {
        HardwareAddress hardwareAddress = HardwareAddressFactory.getInstance().fromConfigXML(tmpStr);
        commandTagCacheObject.setHardwareAddress(hardwareAddress);
        setUpdateHardwareAddress(hardwareAddress, commandTagUpdate);
      }
      catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Exception: Unable to create HardwareAddress from parameter \"hardwareAddress\": " + tmpStr);
      }
    }

    // minValue (Comparable)
    if ((tmpStr = properties.getProperty("minValue")) != null) {
      Comparable comparableMin = (Comparable) TypeConverter.cast(tmpStr, commandTagCacheObject.getDataType());
      commandTagCacheObject.setMinimum(comparableMin);
    }

    // Try to extract maxValue of the appropriate data type
    if ((tmpStr = properties.getProperty("maxValue")) != null) {
      Comparable comparableMax = (Comparable) TypeConverter.cast(tmpStr, commandTagCacheObject.getDataType());
      commandTagCacheObject.setMaximum(comparableMax);
    }

    RbacAuthorizationDetails authorizationDetails = new RbacAuthorizationDetails();
    boolean configureAuthorization = false;
    if ((tmpStr = properties.getProperty("rbacClass")) != null) {
      authorizationDetails.setRbacClass(tmpStr);
      configureAuthorization = true;
    }
    else if (commandTagCacheObject.getAuthorizationDetails() != null) {
      authorizationDetails.setRbacClass(commandTagCacheObject.getAuthorizationDetails().getRbacClass());
    }
    if ((tmpStr = properties.getProperty("rbacDevice")) != null) {
      authorizationDetails.setRbacDevice(tmpStr);
      configureAuthorization = true;
    }
    else if (commandTagCacheObject.getAuthorizationDetails() != null) {
      authorizationDetails.setRbacDevice(commandTagCacheObject.getAuthorizationDetails().getRbacDevice());
    }
    if ((tmpStr = properties.getProperty("rbacProperty")) != null) {
      authorizationDetails.setRbacProperty(tmpStr);
      configureAuthorization = true;
    }
    else if (commandTagCacheObject.getAuthorizationDetails() != null) {
      authorizationDetails.setRbacProperty(commandTagCacheObject.getAuthorizationDetails().getRbacProperty());
    }
    if (configureAuthorization) {
      commandTagCacheObject.setAuthorizationDetails(authorizationDetails);
    }

    // equipmentId (Long) - not currently used, as need to remove and add for this change
    if ((tmpStr = properties.getProperty("equipmentId")) != null) {
      commandTagCacheObject.setEquipmentId(parseLong(tmpStr, "equipmentId"));
      commandTagCacheObject.setProcessId(equipmentCache.get(commandTagCacheObject.getEquipmentId()).getProcessId());

    }
    return commandTagUpdate;
  }

  @Override
  public void validateConfig(CommandTag commandTag) throws ConfigurationException {
    if (commandTag.getId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
    }
    if (commandTag.getName() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be null");
    }
    if (commandTag.getName().length() == 0 || commandTag.getName().length() > 60) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" must be 1 to 60 characters long");
    }
    if (commandTag.getDescription() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" cannot be null");
    }
    if (commandTag.getDescription().length() > 100) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" can be up to 100 characters long");
    }
    switch (commandTag.getMode()) {
      case DataTagConstants.MODE_OPERATIONAL:
        break;
      case DataTagConstants.MODE_TEST:
        break;
      case DataTagConstants.MODE_MAINTENANCE:
        break;
      default:
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Invalid value for parameter \"mode\" : " + commandTag.getMode());
    }

    if (commandTag.getDataType() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"dataType\" cannot be null");
    }

    if (commandTag.getSourceRetries() < 0 || commandTag.getSourceRetries() > 3) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"sourceRetries\" must be between 0 and 3");
    }

    if (commandTag.getSourceTimeout() < 100) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"sourceTimeout\" must be >= 100");
    }

    if (commandTag.getClientTimeout() < 5000) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"clientTimeout\" must be >= 5000");
    }

    if (commandTag.getClientTimeout() < commandTag.getExecTimeout()) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"clientTimeout\" must be greater than execTimeout");
    }

    if (commandTag.getExecTimeout() < (commandTag.getSourceTimeout() * (commandTag.getSourceRetries() + 1))) {
      log.debug("sourceTimeout: " + commandTag.getSourceTimeout() + " sourceRetries: " + commandTag.getSourceRetries() + " execTimeout: " + commandTag.getExecTimeout());
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"execTimeout\" must be greater than (sourceRetries + 1) * sourceTimeout");
    }

    if (commandTag.getMinimum() != null) {
      try {
        Class minValueClass = TypeConverter.getType(commandTag.getDataType());
        if (!minValueClass.isInstance(commandTag.getMinimum())) {
          throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"mininum\" must be of type " + commandTag.getDataType() + " or null");
        }
      }
      catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Error validating parameter \"minimum\": " + e.getMessage());
      }

    }

    if (commandTag.getMaximum() != null) {
      try {
        Class maxValueClass = TypeConverter.getType(commandTag.getDataType());
        if (!maxValueClass.isInstance(commandTag.getMaximum())) {
          throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"maximum\" must be of type " + commandTag.getDataType() + " or null");
        }
      }
      catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Error validating parameter \"maximum\": " + e.getMessage());
      }

    }

    if (commandTag.getEquipmentId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"equipmentId\" cannot be null");
    }
    if (commandTag.getAuthorizationDetails() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Authorization details cannot be null.");
    }
  }

  /**
   * Sets the {@link HardwareAddress} field in the {@link CommandTagUpdate}.
   *
   * @param hardwareAddress  the new {@link HardwareAddress}
   * @param commandTagUpdate the update object that will be sent to the DAQ
   *
   * @throws IllegalAccessException
   * @throws
   */
  private void setUpdateHardwareAddress(HardwareAddress hardwareAddress, CommandTagUpdate commandTagUpdate) throws IllegalAccessException {
    HardwareAddressUpdate hardwareAddressUpdate = new HardwareAddressUpdate(hardwareAddress.getClass().getName());
    commandTagUpdate.setHardwareAddressUpdate(hardwareAddressUpdate);
    SimpleTypeReflectionHandler reflectionHandler = new SimpleTypeReflectionHandler();
    for (Field field : reflectionHandler.getNonTransientSimpleFields(hardwareAddress.getClass())) {
      field.setAccessible(true);
      hardwareAddressUpdate.getChangedValues().put(field.getName(), field.get(hardwareAddress));
    }
  }
}
