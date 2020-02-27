package cern.c2mon.cache.actions.datatag;

import cern.c2mon.cache.actions.equipment.BaseEquipmentServiceImpl;
import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.cache.actions.subequipment.SubEquipmentService;
import cern.c2mon.cache.config.tag.AbstractTagCacheObjectFactory;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.common.SimpleTypeReflectionHandler;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagDeadband;
import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.common.validation.MicroValidator;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.DataTagAddressUpdate;
import cern.c2mon.shared.daq.config.DataTagUpdate;
import cern.c2mon.shared.daq.config.HardwareAddressUpdate;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.util.Properties;


/**
 * @author Szymon Halastra, Alexandros Papageorgiou
 */
@Slf4j
@Named
public class DataTagCacheObjectFactory extends AbstractTagCacheObjectFactory<DataTag> {
  private final BaseEquipmentServiceImpl equipmentService;
  private SubEquipmentService subEquipmentService;

  @Inject
  public DataTagCacheObjectFactory(EquipmentService equipmentService, SubEquipmentService subEquipmentService) {
    this.equipmentService = equipmentService;
    this.subEquipmentService = subEquipmentService;
  }

  @Override
  public DataTag createCacheObject(Long id) {
    return new DataTagCacheObject(id);
  }

  @Override
  public void validateConfig(DataTag dataTag) throws ConfigurationException {
    super.validateConfig(dataTag);
    new MicroValidator<>(dataTag)
      //DataTag must have equipment or subequipment id set
      .not(dataTagObj -> dataTagObj.getEquipmentId() == null && dataTagObj.getSubEquipmentId() == null,
        "Equipment/SubEquipment id not set for DataTag with id " + dataTag.getId() + " - unable to configure it.")
      .optType(DataTag::getMinValue, dataTag.getDataType(), "\"minValue\"")
      .optType(DataTag::getMaxValue, dataTag.getDataType(), "\"maxValue\"")
      .notNull(DataTag::getAddress, "address");

    dataTag.getAddress().validate();
  }


  @Override
  public Change configureCacheObject(DataTag tag, Properties properties) {
    super.configureCacheObject(tag, properties);

    DataTagCacheObject dataTagCacheObject = (DataTagCacheObject) tag;
    DataTagUpdate dataTagUpdate = setCommonProperties(dataTagCacheObject, properties);

    // TAG equipment identifier
    // Ignore the equipment id for control tags as control tags are INDIRECTLY
    // referenced via the equipment's aliveTag and commFaultTag fields
    if (equipmentService != null) {

      // Only one of equipment / subequipment Id should be set. But if both are there, we will overwrite the process ID
      // with the equipment (more important)
      new PropertiesAccessor(properties)
        .getLong("subEquipmentId").ifPresent(subEquipmentId -> {
        dataTagCacheObject.setSubEquipmentId(subEquipmentId);
        dataTagCacheObject.setProcessId(subEquipmentService.getProcessId(subEquipmentId));
      }).getLong("equipmentId").ifPresent(equipmentId -> {
        dataTagCacheObject.setEquipmentId(equipmentId);
        dataTagCacheObject.setProcessId(equipmentService.getProcessId(equipmentId));
      });
    }

    new PropertiesAccessor(properties)
      .getAs("minValue", prop -> (Comparable) TypeConverter.cast("null".equals(prop) ? null : prop, dataTagCacheObject.getDataType()))
      .ifPresent(minValue -> {
        dataTagCacheObject.setMinValue(minValue);
        dataTagUpdate.setMinValue((Number) minValue);
      }).getAs("maxValue", prop -> (Comparable) TypeConverter.cast("null".equals(prop) ? null : prop, dataTagCacheObject.getDataType()))
      .ifPresent(maxValue -> {
        dataTagCacheObject.setMaxValue(maxValue);
        dataTagUpdate.setMaxValue((Number) maxValue);
      }).getAs("address", DataTagAddress::fromConfigXML).ifPresent(dataTagAddress -> {
      dataTagCacheObject.setAddress(dataTagAddress);
      try {
        setUpdateDataTagAddress(dataTagAddress, dataTagUpdate);
      } catch (IllegalAccessException e) {
        log.debug("Failed to update datatag address ", e);
      }
    });

    if (dataTagCacheObject.getEquipmentId() != null)
      dataTagUpdate.setEquipmentId(dataTagCacheObject.getEquipmentId());

    return dataTagUpdate;
  }

  /**
   * Notice only non-null properties are set, the others staying unaffected
   * by this method.
   *
   * @return the returned update object with changes that need sending to the
   * DAQ (only used when reconfiguring a Data/ControlTag, not rules)
   * IMPORTANT: the change id and equipment id still needs setting on the returned object
   * in the DataTag-specific facade
   * @throws ConfigurationException
   */
  private DataTagUpdate setCommonProperties(AbstractTagCacheObject tag, Properties properties)
    throws ConfigurationException {

    DataTagUpdate innerDataTagUpdate = new DataTagUpdate();
    innerDataTagUpdate.setDataTagId(tag.getId());

    new PropertiesAccessor(properties)
      .getString("name").ifPresent(name -> {
      tag.setName(name);
      innerDataTagUpdate.setName(name);
    }).getString("description").ifPresent(description -> {
      tag.setDescription(description);
      innerDataTagUpdate.setName(description);
    }).getString("dataType").ifPresent(dataType -> {
      tag.setDataType(dataType);
      innerDataTagUpdate.setDataType(dataType);
    }).getShort("mode").ifPresent(mode -> {
      tag.setMode(mode);
      innerDataTagUpdate.setMode(mode);
    });

    return innerDataTagUpdate;
  }

  /**
   * Sets the DataTagAddress part of an update from the XML String.
   *
   * @param dataTagAddress the new address
   * @param dataTagUpdate  the update object for which the address needs setting
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  private void setUpdateDataTagAddress(final DataTagAddress dataTagAddress, final DataTagUpdate dataTagUpdate) throws IllegalArgumentException, IllegalAccessException {
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
    } else {
      dataTagAddressUpdate.addFieldToRemove("valueDeadbandType");
      dataTagAddressUpdate.addFieldToRemove("valueDeadband");
    }
    if (dataTagAddress.getTimeDeadband() != DataTagDeadband.DEADBAND_NONE) {
      dataTagAddressUpdate.setTimeDeadband(dataTagAddress.getTimeDeadband());
    } else {
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
}
