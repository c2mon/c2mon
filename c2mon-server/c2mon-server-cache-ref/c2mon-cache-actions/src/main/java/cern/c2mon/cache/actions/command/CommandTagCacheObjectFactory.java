package cern.c2mon.cache.actions.command;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.common.SimpleTypeReflectionHandler;
import cern.c2mon.shared.common.command.CommandTag;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.common.validation.MicroValidator;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.CommandTagUpdate;
import cern.c2mon.shared.daq.config.HardwareAddressUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static cern.c2mon.shared.common.datatag.DataTagConstants.MODE_OPERATIONAL;
import static cern.c2mon.shared.common.datatag.DataTagConstants.MODE_TEST;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Component
public class CommandTagCacheObjectFactory extends AbstractCacheObjectFactory<CommandTag> {

  private C2monCache<Equipment> equipmentCacheRef;

//  @Inject
//  public CommandCacheObjectFactory(C2monCache<Long, Equipment> equipmentCacheRef) {
//    this.equipmentCacheRef = equipmentCacheRef;
//  }

  @Override
  public CommandTag createCacheObject(Long id) {
    CommandTagCacheObject commandTagCacheObject = new CommandTagCacheObject(id);

    return commandTagCacheObject;
  }

  @Override
  public Change configureCacheObject(CommandTag commandTag, Properties properties) {
    CommandTagCacheObject commandTagCacheObject = (CommandTagCacheObject) commandTag;
    CommandTagUpdate commandTagUpdate = new CommandTagUpdate();
    commandTagUpdate.setCommandTagId(commandTag.getId());

    new PropertiesAccessor(properties)
      .getString("name").ifPresent(name -> {
      commandTagCacheObject.setName(name);
      commandTagUpdate.setName(name);
    })
      .getString("description").ifPresent(commandTagCacheObject::setDescription)
      .getShort("mode").ifPresent(commandTagCacheObject::setMode)
      .getString("dataType").ifPresent(commandTagCacheObject::setDataType)
      .getInteger("sourceRetries").ifPresent(sourceRetries -> {
      commandTagCacheObject.setSourceRetries(sourceRetries);
      commandTagUpdate.setSourceRetries(sourceRetries);
    }).getInteger("sourceTimeout").ifPresent(sourceTimeout -> {
      commandTagCacheObject.setSourceTimeout(sourceTimeout);
      commandTagUpdate.setSourceTimeout(sourceTimeout);
    }).getInteger("execTimeout").ifPresent(commandTagCacheObject::setExecTimeout)
      .getInteger("clientTimeout").ifPresent(commandTagCacheObject::setClientTimeout)
      .getAs("hardwareAddress", HardwareAddressFactory.getInstance()::fromConfigXML).ifPresent(hardwareAddress -> {
      commandTagCacheObject.setHardwareAddress(hardwareAddress);
      try {
        setUpdateHardwareAddress(hardwareAddress, commandTagUpdate);
      } catch (IllegalAccessException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
          "Exception: Unable to create HardwareAddress from parameter \"hardwareAddress\": " + hardwareAddress);
      }
    }).getString("minValue").ifPresent(minValue -> {
      Comparable comparableMin = (Comparable) TypeConverter.cast(minValue, commandTagCacheObject.getDataType());
      commandTagCacheObject.setMinimum(comparableMin);
    }).getString("maxValue").ifPresent(maxValue -> {
      Comparable comparableMin = (Comparable) TypeConverter.cast(maxValue, commandTagCacheObject.getDataType());
      commandTagCacheObject.setMaximum(comparableMin);
    }).getLong("equipmentId").ifPresent(equipmentId -> {
      commandTagCacheObject.setEquipmentId(equipmentId);
      commandTagCacheObject.setProcessId(equipmentCacheRef.get(equipmentId).getProcessId());
    });

    RbacAuthorizationDetails rbacAuthorizationDetails = createRbacAuthorizationDetails(commandTagCacheObject, properties);

    if (rbacAuthorizationDetails != null) {
      commandTagCacheObject.setAuthorizationDetails(rbacAuthorizationDetails);
    }

    return commandTagUpdate;
  }

  @Override
  public void validateConfig(CommandTag commandTag) throws ConfigurationException {

    new MicroValidator<>(commandTag)
      .notNull(Cacheable::getId, "id")
      .notNull(CommandTag::getName, "name")
      .between(tag -> tag.getName().length(), 0, 60, "Parameter \"name\" must be 1 to 60 characters long")
      .notNull(CommandTag::getDescription, "description")
      .between(tag -> tag.getDescription().length(), 0, 100, "Parameter \"description\" can be up to 100 characters long")
      .between(CommandTag::getMode, MODE_OPERATIONAL, MODE_TEST, "Invalid value for parameter \"mode\" : " + commandTag.getMode())
      .notNull(CommandTag::getDataType, "dataType")
      .between(CommandTag::getSourceRetries, 0, 3, "Parameter \"sourceRetries\" must be between 0 and 3")
      .between(CommandTag::getSourceTimeout, 100, Integer.MAX_VALUE, "Parameter \"sourceTimeout\" must be >= 100")
      .between(CommandTag::getClientTimeout, 5000, Integer.MAX_VALUE, "Parameter \"clientTimeout\" must be >= 5000")
      .between(CommandTag::getClientTimeout, commandTag.getExecTimeout(), Integer.MAX_VALUE, "Parameter \"clientTimeout\" must be greater than execTimeout")
      .notNull(CommandTag::getEquipmentId, "equipmentId")
      .notNull(CommandTag::getAuthorizationDetails, "AuthorizationDetails")
      .between(CommandTag::getExecTimeout, commandTag.getSourceTimeout() * (commandTag.getSourceRetries() + 1), Integer.MAX_VALUE,
        "Parameter \"execTimeout\" must be greater than (sourceRetries + 1) * sourceTimeout")
      .optType(CommandTag::getMinimum, commandTag.getDataType(), "minimum")
      .optType(CommandTag::getMaximum, commandTag.getDataType(), "maximum");
  }

  private RbacAuthorizationDetails createRbacAuthorizationDetails(CommandTagCacheObject commandTagCacheObject, Properties properties) {
    RbacAuthorizationDetails authorizationDetails = new RbacAuthorizationDetails();

    if (commandTagCacheObject.getAuthorizationDetails() != null) {
      authorizationDetails.setRbacClass(commandTagCacheObject.getAuthorizationDetails().getRbacClass());
      authorizationDetails.setRbacDevice(commandTagCacheObject.getAuthorizationDetails().getRbacDevice());
      authorizationDetails.setRbacProperty(commandTagCacheObject.getAuthorizationDetails().getRbacProperty());
    }

    final AtomicBoolean configureAuthorization = new AtomicBoolean(false);
    new PropertiesAccessor(properties)
      .getString("rbacClass").ifPresent(rbacClass -> {
      authorizationDetails.setRbacClass(rbacClass);
      configureAuthorization.set(true);
    }).getString("rbacDevice").ifPresent(rbacDevice -> {
      authorizationDetails.setRbacDevice(rbacDevice);
      configureAuthorization.set(true);
    }).getString("rbacProperty").ifPresent(rbacProperty -> {
      authorizationDetails.setRbacProperty(rbacProperty);
      configureAuthorization.set(true);
    });

    return configureAuthorization.get() ? authorizationDetails : null;
  }

  /**
   * Sets the {@link HardwareAddress} field in the {@link CommandTagUpdate}.
   *
   * @param hardwareAddress  the new {@link HardwareAddress}
   * @param commandTagUpdate the update object that will be sent to the DAQ
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
