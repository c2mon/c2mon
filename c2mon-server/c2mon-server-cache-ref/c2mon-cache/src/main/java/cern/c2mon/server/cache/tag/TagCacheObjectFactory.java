package cern.c2mon.server.cache.tag;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.CacheObjectFactory;
import cern.c2mon.server.cache.CoreAbstractEquipmentService;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.metadata.Metadata;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.SimpleTypeReflectionHandler;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagDeadband;
import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.DataTagAddressUpdate;
import cern.c2mon.shared.daq.config.DataTagUpdate;
import cern.c2mon.shared.daq.config.HardwareAddressUpdate;

/**
 * Creates {@link DataTag} cache object
 * {@link DataTag} contains common interface for DataTag and ControlTag
 * apparently they are differ only by class name, methods definitions are the same
 *
 * @author Szymon Halastra
 */
public abstract class TagCacheObjectFactory<T extends Tag> extends CacheObjectFactory<T> {

  private final Cache<Long, T> tagCacheRef;

  private final CoreAbstractEquipmentService coreAbstractEquipmentService;

  public TagCacheObjectFactory(Cache<Long, T> tagCacheRef, CoreAbstractEquipmentService coreAbstractEquipmentService) {
    this.tagCacheRef = tagCacheRef;
    this.coreAbstractEquipmentService = coreAbstractEquipmentService;
  }

  @Override
  public Change configureCacheObject(T tag, Properties properties) throws ConfigurationException, IllegalArgumentException, IllegalAccessException {
    DataTagCacheObject dataTagCacheObject = (DataTagCacheObject) tag;
    DataTagUpdate dataTagUpdate = setCommonProperties(dataTagCacheObject, properties);
    String tmpStr;

    // TAG equipment identifier
    // Ignore the equipment id for control tags as control tags are INDIRECTLY
    // referenced via the equipment's aliveTag and commFaultTag fields
    if (coreAbstractEquipmentService != null && !(dataTagCacheObject instanceof ControlTag)) {
      if ((tmpStr = properties.getProperty("equipmentId")) != null) {
        try {
          dataTagCacheObject.setEquipmentId(Long.valueOf(tmpStr));
          dataTagCacheObject.setProcessId(coreAbstractEquipmentService.getProcessIdForAbstractEquipment(dataTagCacheObject.getEquipmentId()));
        }
        catch (NumberFormatException e) {
          throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: "
                  + "Unable to convert parameter \"equipmentId\" to Long: " + tmpStr);
        }
      }

      // TIMS-951: Allow attachment of DataTags to SubEquipments
      else if ((tmpStr = properties.getProperty("subEquipmentId")) != null) {
        try {
          dataTagCacheObject.setSubEquipmentId(Long.valueOf(tmpStr));
          dataTagCacheObject.setProcessId(coreAbstractEquipmentService.getProcessIdForAbstractEquipment(dataTagCacheObject.getSubEquipmentId()));
        }
        catch (NumberFormatException e) {
          throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: "
                  + "Unable to convert parameter \"subEquipmentId\" to Long: " + tmpStr);
        }
      }
    }

    if ((tmpStr = properties.getProperty("minValue")) != null) {
      if (tmpStr.equals("null")) {
        dataTagCacheObject.setMinValue(null);
        dataTagUpdate.setMinValue(null);
      }
      else {
        Comparable comparableMin = (Comparable) TypeConverter.cast(tmpStr, dataTagCacheObject.getDataType());
        dataTagCacheObject.setMinValue(comparableMin);
        dataTagUpdate.setMinValue((Number) comparableMin);
      }
    }

    if ((tmpStr = properties.getProperty("maxValue")) != null) {
      if (tmpStr.equals("null")) {
        dataTagCacheObject.setMaxValue(null);
        dataTagUpdate.setMaxValue(null);
      }
      else {
        Comparable comparableMax = (Comparable) TypeConverter.cast(tmpStr, dataTagCacheObject.getDataType());
        dataTagCacheObject.setMaxValue(comparableMax);
        dataTagUpdate.setMaxValue((Number) comparableMax);
      }
    }

    // TAG address
    tmpStr = properties.getProperty("address");
    if (tmpStr != null) {
      DataTagAddress dataTagAddress = DataTagAddress.fromConfigXML(tmpStr);
      dataTagCacheObject.setAddress(dataTagAddress);
      setUpdateDataTagAddress(dataTagAddress, dataTagUpdate);
    }

    if (dataTagCacheObject.getEquipmentId() != null)
      dataTagUpdate.setEquipmentId(dataTagCacheObject.getEquipmentId());

    return dataTagUpdate;
  }

  /**
   * TODO set JMS client topic still needs doing
   * <p>
   * Sets the fields of the AbstractTagCacheObject from the Properties object.
   * Notice only non-null properties are set, the others staying unaffected
   * by this method.
   *
   * @param tag
   * @param properties
   *
   * @return the returned update object with changes that need sending to the
   * DAQ (only used when reconfiguring a Data/ControlTag, not rules)
   * IMPORTANT: the change id and equipment id still needs setting on the returned object
   * in the DataTag-specific facade
   * @throws ConfigurationException
   */
  protected DataTagUpdate setCommonProperties(AbstractTagCacheObject tag, Properties properties)
          throws ConfigurationException {

    Optional<DataTagUpdate> dataTagUpdate = tagCacheRef.executeTransaction(() -> {
      DataTagUpdate innerDataTagUpdate = new DataTagUpdate();
      innerDataTagUpdate.setDataTagId(tag.getId());

      String tmpStr = null;

      // TAG name and topic derived from name
      if ((tmpStr = properties.getProperty("name")) != null) {
        tag.setName(tmpStr);
        innerDataTagUpdate.setName(tmpStr);
        //this.topic = getTopicForName(this.name);
      }

      // TAG description
      if ((tmpStr = properties.getProperty("description")) != null) {
        tag.setDescription(tmpStr);
        innerDataTagUpdate.setName(tmpStr);
      }


      // TAG data type
      if ((tmpStr = properties.getProperty("dataType")) != null) {
        tag.setDataType(tmpStr);
        innerDataTagUpdate.setDataType(tmpStr);
      }


      // TAG mode
      if ((tmpStr = properties.getProperty("mode")) != null) {
        try {
          tag.setMode(Short.parseShort(tmpStr));
          innerDataTagUpdate.setMode(Short.parseShort(tmpStr));
        }
        catch (NumberFormatException e) {
          throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"mode\" to short: " + tmpStr);
        }
      }

      // TAG log flag
      if ((tmpStr = properties.getProperty("isLogged")) != null) {
        tag.setLogged(tmpStr.equalsIgnoreCase("true"));
      }

      // TAG unit
      tmpStr = properties.getProperty("unit");
      if (tmpStr != null) {
        tag.setUnit(checkAndSetNull(tmpStr));
      }

      // DIP address
      if (properties.getProperty("dipAddress") != null) {
        tag.setDipAddress(checkAndSetNull(properties.getProperty("dipAddress")));
      }

      // JAPC address
      if (properties.getProperty("japcAddress") != null) {
        tag.setJapcAddress(checkAndSetNull(properties.getProperty("japcAddress")));
      }

      // TAG metadata
      tmpStr = properties.getProperty("metadata");
      if (tmpStr != null) {
        Metadata clientMetadata = Metadata.fromJSON(tmpStr);

        if (clientMetadata.isUpdate()) {
          if (!clientMetadata.getRemoveList().isEmpty()) {
            for (String key : clientMetadata.getRemoveList()) {
              tag.getMetadata().getMetadata().remove(key);
            }
          }
          for (Map.Entry<String, Object> entry : clientMetadata.getMetadata().entrySet()) {
            tag.getMetadata().addMetadata(entry.getKey(), entry.getValue());
          }
        }
        else {
          cern.c2mon.server.common.metadata.Metadata metadata = new cern.c2mon.server.common.metadata.Metadata();
          metadata.setMetadata(clientMetadata.getMetadata());
          tag.setMetadata(metadata);
        }
      }

      return innerDataTagUpdate;
    });

    return dataTagUpdate.get();
  }

  /**
   * Sets the DataTagAddress part of an update from the XML String.
   *
   * @param dataTagAddress the new address
   * @param dataTagUpdate  the update object for which the address needs setting
   *
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  protected void setUpdateDataTagAddress(final DataTagAddress dataTagAddress, final DataTagUpdate dataTagUpdate) throws IllegalArgumentException, IllegalAccessException {
    DataTagAddressUpdate dataTagAddressUpdate = new DataTagAddressUpdate();
    dataTagUpdate.setDataTagAddressUpdate(dataTagAddressUpdate);
    dataTagAddressUpdate.setGuaranteedDelivery(dataTagAddress.isGuaranteedDelivery());
    dataTagAddressUpdate.setPriority(dataTagAddress.getPriority());
    if (dataTagAddress.getTimeToLive() != DataTagAddress.TTL_FOREVER) {
      dataTagAddressUpdate.setTimeToLive(dataTagAddress.getTimeToLive());
    }
    if (dataTagAddress.getValueDeadbandType() != DataTagDeadband.DEADBAND_NONE) {
      dataTagAddressUpdate.setValueDeadbandType(dataTagAddress.getValueDeadbandType());
      dataTagAddressUpdate.setValueDeadband(dataTagAddress.getValueDeadband());
    }
    else {
      dataTagAddressUpdate.addFieldToRemove("valueDeadbandType");
      dataTagAddressUpdate.addFieldToRemove("valueDeadband");
    }
    if (dataTagAddress.getTimeDeadband() != DataTagDeadband.DEADBAND_NONE) {
      dataTagAddressUpdate.setTimeDeadband(dataTagAddress.getTimeDeadband());
    }
    else {
      dataTagAddressUpdate.addFieldToRemove("timeDeadband");
    }
    if (dataTagAddress.getHardwareAddress() != null) {
      HardwareAddressUpdate hardwareAddressUpdate = new HardwareAddressUpdate(dataTagAddress.getHardwareAddress().getClass().getName());
      dataTagAddressUpdate.setHardwareAddressUpdate(hardwareAddressUpdate);
      SimpleTypeReflectionHandler reflectionHandler = new SimpleTypeReflectionHandler();
      for (Field field : reflectionHandler.getNonTransientSimpleFields(dataTagAddress.getHardwareAddress().getClass())) {
        field.setAccessible(true);
        hardwareAddressUpdate.getChangedValues().put(field.getName(), field.get(dataTagAddress.getHardwareAddress()));
      }
    }
    if (dataTagAddress.getFreshnessInterval() != null) {
      dataTagAddressUpdate.setFreshnessInterval(dataTagAddress.getFreshnessInterval());
    }
  }

  /**
   * Checks that the AbstractTagCacheObject passes all validation tests for
   * being included in TIM. This method should be called during runtime
   * reconfigurations for instance.
   * <p>
   * TODO commented out desc and dictionary null checks below (as test server does not satisfy these) - introduce them again for operation?
   *
   * @param tag the tag to validate
   *
   * @throws ConfigurationException if a validation test fails
   */
  protected void validateTagConfig(final T tag) throws ConfigurationException {
    if (tag.getId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
    }
    if (tag.getName() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be null");
    }
    if (tag.getName().length() == 0) { //|| tag.getName().length() > 60
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be empty");
    }
//      if (tag.getDescription() == null) {
//        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" cannot be null");
//      }
//      if (tag.getDescription().length() == 0 || tag.getDescription().length() > 100) {
//        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" must be 1 to 100 characters long");
//      }
    if (tag.getDataType() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"dataType\" cannot be null");
    }
//      if (tag.getValueDictionary() == null) {
//        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"valueDictionary\" cannot be null");
//      }
    if (tag.getMode() != DataTagConstants.MODE_OPERATIONAL && tag.getMode() != DataTagConstants.MODE_TEST && tag.getMode() != DataTagConstants.MODE_MAINTENANCE) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Invalid value for parameter \"mode\".");
    }
    //TODO setting client topic still needs doing...
//      if (tag.getTopic() == null) {
//        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"topic\" cannot be null");
//      }
    if (tag.getUnit() != null) {
      if (tag.getUnit().length() > 20) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"unit\" must be 0 to 20 characters long");
      }
    }
  }
}
