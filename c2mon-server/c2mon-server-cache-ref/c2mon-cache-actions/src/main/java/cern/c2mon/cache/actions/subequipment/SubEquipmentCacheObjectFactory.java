package cern.c2mon.cache.actions.subequipment;

import cern.c2mon.cache.actions.equipment.AbstractEquipmentCacheObjectFactory;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
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
public class SubEquipmentCacheObjectFactory extends AbstractEquipmentCacheObjectFactory<SubEquipment> {

  @Override
  public SubEquipment createCacheObject(Long id) {
    return new SubEquipmentCacheObject(id);
  }

  /**
   * Overridden as for SubEquipment rule out changing the parent equipment
   * associated it is associated to.
   *
   * @return empty EquipmentConfigurationUpdate because SubEquipments are not used
   * on the DAQ layer and no event is sent (return type necessary as in
   * common interface).
   * @throws IllegalAccessException
   */
  @Override
  public EquipmentConfigurationUpdate updateConfig(final SubEquipment subEquipment,
                                                   final Properties properties) throws IllegalAccessException {
    // TODO: Remove obsolete parent_equip_id property
    if ((properties.getProperty("parent_equip_id")) != null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Reconfiguration of "
              + "SubEquipment does not currently allow it to be reassigned to a different Equipment!");
    }

    if ((properties.getProperty("equipmentId")) != null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Reconfiguration of "
              + "SubEquipment does not currently allow it to be reassigned to a different Equipment!");
    }

    super.updateConfig(subEquipment, properties);
    return new EquipmentConfigurationUpdate();
  }

  /**
   * Throws an exception if the validation fails.
   *
   * @param subEquipment the SubEquipment to validate
   *
   * @throws ConfigurationException if the validation fails
   */
  @Override
  public void validateConfig(final SubEquipment subEquipment) {
    SubEquipmentCacheObject subEquipmentCacheObject = (SubEquipmentCacheObject) subEquipment;
    super.validateConfig(subEquipmentCacheObject);
    new MicroValidator<>(subEquipment)
      .notNull(SubEquipment::getParentId, "parentId");
  }

  /**
   * Sets the fields particular for SubEquipment from the properties object.
   *
   * @param subEquipment sets the fields in this object
   * @param properties   looks for relevant properties in this object
   */
  @Override
  public Change configureCacheObject(SubEquipment subEquipment, Properties properties) {
    SubEquipmentCacheObject subEquipmentCacheObject = (SubEquipmentCacheObject) subEquipment;
    EquipmentConfigurationUpdate update = setCommonProperties(subEquipment, properties);

    new PropertiesAccessor(properties)
      .getLong("parent_equip_id").ifPresent(subEquipmentCacheObject::setParentId)
      .getLong("equipmentId").ifPresent(subEquipmentCacheObject::setParentId);
    // TODO: Remove obsolete parent_equip_id property

    return update;
  }
}
