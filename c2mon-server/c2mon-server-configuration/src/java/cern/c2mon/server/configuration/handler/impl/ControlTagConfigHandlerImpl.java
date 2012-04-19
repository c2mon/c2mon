package cern.c2mon.server.configuration.handler.impl;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.ControlTagConfigTransacted;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.cache.ControlTagCache;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;

/**
 * See interface documentation.
 *  
 * <p>TODO update and remove are same as {@link DataTagConfigHandlerImpl} so could extend it
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
  
  /**
   * Wrapped bean with transacted methods.
   */
  @Autowired
  private ControlTagConfigTransacted controlTagConfigTransacted;
  
  private ControlTagCache controlTagCache;


  @Autowired  
  public ControlTagConfigHandlerImpl(ControlTagCache controlTagCache) {    
    this.controlTagCache = controlTagCache;
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
    return controlTagConfigTransacted.doCreateControlTag(element);
  }

  @Override
  public ProcessChange updateControlTag(Long id, Properties elementProperties) throws IllegalAccessException {
    try {
      return controlTagConfigTransacted.doUpdateControlTag(id, elementProperties); 
    } catch (UnexpectedRollbackException e) {
      LOGGER.error("Rolling back ControlTag update in cache");
      controlTagCache.remove(id);
      controlTagCache.get(id);
      throw e;
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
  
}
