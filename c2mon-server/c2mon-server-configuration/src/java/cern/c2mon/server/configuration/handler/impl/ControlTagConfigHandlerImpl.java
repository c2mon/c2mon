package cern.c2mon.server.configuration.handler.impl;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.ControlTagConfigTransacted;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * See interface documentation.
 * <p>Please note that all changes on control tags will require a lock of the equipment. Otherwise
 * it can lead to a deadlock situation in the running server.
 *  
 * <p>TODO: update and remove are same as {@link DataTagConfigHandlerImpl} so could extend it
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class ControlTagConfigHandlerImpl implements ControlTagConfigHandler {
  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ControlTagConfigHandlerImpl.class);  
  
  /** Variable definition in configuration element for equipment ID */
  private static final String EQUIPMENT_ID = "equipmentId";
  
  private final EquipmentCache equipmentCache;
  
  private final SubEquipmentCache subEquipmentCache;
  
  /**
   * Wrapped bean with transacted methods.
   */
  @Autowired
  private ControlTagConfigTransacted controlTagConfigTransacted;
  
  private ControlTagCache controlTagCache;


  @Autowired  
  public ControlTagConfigHandlerImpl(ControlTagCache controlTagCache, 
                                     EquipmentCache equipmentCache,
                                     SubEquipmentCache subEquipmentCache) {    
    this.controlTagCache = controlTagCache;
    this.equipmentCache = equipmentCache;
    this.subEquipmentCache = subEquipmentCache;
  }
  
  /**
   * Removes the control tag and fills in the passed report in case
   * of failure. The control tag removed is commited if successful.
   * If it fails, a rollback exception is throw and all *local* changes
   * are rolled back (the calling method needs to handle the thrown 
   * UnexpectedRollbackException appropriately.
   * 
   * @param id the id of the tag to remove
   * @param tagReport the report for this removal
   * @return a DAQ configuration event if ControlTag is associated to some Equipment or SubEquipment and DAQ
   *          needs informing (i.e. if has Address) , else return null
   */
  @Override
  public ProcessChange removeControlTag(Long id, ConfigurationElementReport tagReport) {
    ProcessChange change = controlTagConfigTransacted.doRemoveControlTag(id, tagReport);    
    controlTagCache.remove(id); //will be skipped if rollback exception thrown in do method
    return change;
  }

  @Override
  public ProcessChange createControlTag(ConfigurationElement element) throws IllegalAccessException {
    ProcessChange change;
    Long controlTagId = element.getEntityId();
    acquireEquipmentWriteLockForElement(controlTagId, element.getElementProperties());
    try {
      change = controlTagConfigTransacted.doCreateControlTag(element);
    } finally {
      releaseEquipmentWriteLockForElement(controlTagId, element.getElementProperties());
    }
    controlTagCache.lockAndNotifyListeners(controlTagId);
    return change;
  }

  @Override
  public ProcessChange updateControlTag(Long id, Properties elementProperties) throws IllegalAccessException {
    acquireEquipmentWriteLockForElement(id, elementProperties);
    try {
      return controlTagConfigTransacted.doUpdateControlTag(id, elementProperties); 
    } catch (UnexpectedRollbackException e) {
      LOGGER.error("Rolling back ControlTag update in cache");
      controlTagCache.remove(id);
      controlTagCache.loadFromDb(id);
      throw e;
    } finally {
      releaseEquipmentWriteLockForElement(id, elementProperties);
    }
  }
  
  @Override
  public ProcessChange getCreateEvent(Long configId, Long controlTagId, Long equipmentId, Long processId) {
    return controlTagConfigTransacted.getCreateEvent(configId, controlTagId, equipmentId, processId);
  }
  
  @Override
  public void addAlarmToTag(Long tagId, Long alarmId) {
    controlTagConfigTransacted.addAlarmToTag(tagId, alarmId);
  }

  @Override
  public void addRuleToTag(Long tagId, Long ruleId) {
    controlTagConfigTransacted.addRuleToTag(tagId, ruleId);
  }

  @Override
  public void removeAlarmFromTag(Long tagId, Long alarmId) {
    controlTagConfigTransacted.removeAlarmFromTag(tagId, alarmId);
  }

  @Override
  public void removeRuleFromTag(Long tagId, Long ruleId) {
    controlTagConfigTransacted.removeRuleFromTag(tagId, ruleId);
  }
  
  /**
   * Checks whether the equipment id belongs to the equipment or subequipment
   * cache and locks it.
   * @param equipmentId The id of the equipment of subequipment.
   */
  private void acquireEquipmentWriteLock(final Long equipmentId) {
    if (equipmentCache.hasKey(equipmentId)) {
      equipmentCache.acquireWriteLockOnKey(equipmentId);
    }
    else if (subEquipmentCache.hasKey(equipmentId)) {
      subEquipmentCache.acquireWriteLockOnKey(equipmentId);
    }
    else {
      String errorMsg = "Equipment id " + equipmentId + " unknown in in both equipment and subequipment cache.";
      LOGGER.error(errorMsg);
      throw new UnexpectedRollbackException(errorMsg);
    }
  }
  
  private void acquireEquipmentWriteLockForElement(final Long id, Properties elementProperties) {
    String equipmentIdValue = elementProperties.getProperty(EQUIPMENT_ID);
    if (equipmentIdValue == null || equipmentIdValue.equalsIgnoreCase("")) {
      String errorMsg = "Required property '" + EQUIPMENT_ID + "' is missing to create Control Tag " + id;
      LOGGER.error(errorMsg);
      throw new UnexpectedRollbackException(errorMsg);
    }
    Long equipmentId = Long.valueOf(equipmentIdValue);
    acquireEquipmentWriteLock(equipmentId);
  }
  
  /**
   * Checks whether the equipment id belongs to the equipment or subequipment
   * cache and releases the lock on it.
   * @param equipmentId The id of the equipment of subequipment.
   */
  private void releaseEquimentWriteLock(final Long equipmentId) {
    if (equipmentCache.hasKey(equipmentId)) {
      equipmentCache.releaseWriteLockOnKey(equipmentId);
    }
    else if (subEquipmentCache.hasKey(equipmentId)) {
      subEquipmentCache.releaseWriteLockOnKey(equipmentId);
    }
    else {
      String errorMsg = "Equipment id " + equipmentId + " unknown in both equipment and subequipment cache.";
      LOGGER.error(errorMsg);
      throw new UnexpectedRollbackException(errorMsg);
    }
  }
  
  private void releaseEquipmentWriteLockForElement(final Long id, final Properties elementProperties) {
    String equipmentIdValue = elementProperties.getProperty(EQUIPMENT_ID);
    if (equipmentIdValue == null || equipmentIdValue.equalsIgnoreCase("")) {
      String errorMsg = "Required property '" + EQUIPMENT_ID + "' is missing to create Control Tag " + id;
      LOGGER.error(errorMsg);
      throw new UnexpectedRollbackException(errorMsg);
    }
    Long equipmentId = Long.valueOf(equipmentIdValue);
    releaseEquimentWriteLock(equipmentId);
  } 
}
