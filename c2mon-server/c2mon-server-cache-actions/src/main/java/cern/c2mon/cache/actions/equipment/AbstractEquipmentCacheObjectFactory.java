package cern.c2mon.cache.actions.equipment;

import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.common.equipment.AbstractEquipmentCacheObject;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.common.validation.MicroValidator;
import cern.c2mon.shared.daq.config.EquipmentConfigurationUpdate;

import java.util.Properties;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou
 */
public abstract class AbstractEquipmentCacheObjectFactory<T extends AbstractEquipment> extends AbstractCacheObjectFactory<T> {

  @Override
  public void validateConfig(T abstractEquipment) throws ConfigurationException {
    new MicroValidator<>(abstractEquipment)
      .notNull(AbstractEquipment::getId, "id")
      .notNull(AbstractEquipment::getName, "name")
      .between(eqObj -> eqObj.getName().length(), 0, 60, "name")
      .notNull(AbstractEquipment::getDescription, "description")
      .between(eqObj -> eqObj.getDescription().length(), 1, 100)
      .notNull(AbstractEquipment::getStateTagId, "stateTagId")
      .notNull(AbstractEquipment::getCommFaultTagId, "commFaultTagId")
      .between(AbstractEquipment::getAliveInterval, 10000, Integer.MAX_VALUE);
  }

  /**
   * Sets the common properties for Equipment and Subequipment cache objects
   * when creating or updating them.
   *
   * @param abstractEquipment
   * @param properties
   */
  protected EquipmentConfigurationUpdate setCommonProperties(T abstractEquipment, Properties properties) {
    AbstractEquipmentCacheObject abstractEquipmentCacheObject = (AbstractEquipmentCacheObject) abstractEquipment;
    EquipmentConfigurationUpdate configurationUpdate = new EquipmentConfigurationUpdate();

    configurationUpdate.setEquipmentId(abstractEquipment.getId());

    new PropertiesAccessor(properties)
      .getString("name").ifPresent(name -> {
      abstractEquipmentCacheObject.setName(name);
      configurationUpdate.setName(name);
    }).getString("description").ifPresent(abstractEquipmentCacheObject::setDescription)
      .getLong("aliveTagId").ifPresent(aliveId -> {
      abstractEquipmentCacheObject.setAliveTagId(aliveId);
      configurationUpdate.setAliveTagId(aliveId);
    }).getInteger("aliveInterval").ifPresent(aliveInterval -> {
      abstractEquipmentCacheObject.setAliveInterval(aliveInterval);
      configurationUpdate.setAliveInterval((long) aliveInterval);
    }).getString("handlerClass").ifPresent(abstractEquipmentCacheObject::setHandlerClassName)
      .getLong("commFaultTagId").ifPresent(commFaultTagId -> {
      abstractEquipmentCacheObject.setCommFaultTagId(commFaultTagId);
      configurationUpdate.setCommfaultTagId(commFaultTagId);
    }).getLong("statusTagId").ifPresent(abstractEquipmentCacheObject::setStateTagId)
      // One of the two will be set
      .getLong("stateTagId").ifPresent(abstractEquipmentCacheObject::setStateTagId);

    return configurationUpdate;
  }
}
