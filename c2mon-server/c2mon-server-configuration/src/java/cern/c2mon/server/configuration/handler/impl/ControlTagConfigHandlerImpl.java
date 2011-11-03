package cern.c2mon.server.configuration.handler.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.configuration.handler.AlarmConfigHandler;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.RuleTagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.cache.ControlTagCache;
import cern.tim.server.cache.ControlTagFacade;
import cern.tim.server.cache.DataTagFacade;
import cern.tim.server.cache.EquipmentFacade;
import cern.tim.server.cache.ProcessFacade;
import cern.tim.server.cache.SubEquipmentFacade;
import cern.tim.server.cache.TagLocationService;
import cern.tim.server.cache.exception.CacheElementNotFoundException;
import cern.tim.server.cache.loading.ControlTagLoaderDAO;
import cern.tim.server.common.control.ControlTag;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.client.configuration.ConfigConstants.Action;
import cern.tim.shared.client.configuration.ConfigConstants.Entity;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.daq.config.Change;
import cern.tim.shared.daq.config.DataTagAdd;
import cern.tim.shared.daq.config.DataTagRemove;
import cern.tim.shared.daq.config.DataTagUpdate;
import cern.tim.shared.daq.config.ITagChange;

/**
 * See interface documentation.
 *  
 * <p>TODO update and remove are same as {@link DataTagConfigHandlerImpl} so could extend it
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class ControlTagConfigHandlerImpl extends TagConfigHandlerImpl<ControlTag> implements ControlTagConfigHandler {
  
  private static final Logger LOGGER = Logger.getLogger(ControlTagConfigHandlerImpl.class);
 
  private DataTagFacade dataTagFacade;
  
  private EquipmentFacade equipmentFacade;
  
  private SubEquipmentFacade subEquipmentFacade; 
  
  private ProcessFacade processFacade;
  
  @Autowired
  private RuleTagConfigHandler ruleTagConfigHandler;
  
  @Autowired
  private AlarmConfigHandler alarmConfigHandler;
  
  
  @Autowired
  public ControlTagConfigHandlerImpl(ControlTagCache controlTagCache,
      ControlTagFacade controlTagFacade, DataTagFacade dataTagFacade,
      EquipmentFacade equipmentFacade,
      ControlTagLoaderDAO controlTagLoaderDAO, TagLocationService tagLocationService,
      SubEquipmentFacade subEquipmentFacade, ProcessFacade processFacade) {
    super(controlTagLoaderDAO, controlTagFacade, controlTagCache, tagLocationService);    
    this.dataTagFacade = dataTagFacade;
    this.equipmentFacade = equipmentFacade; 
    this.subEquipmentFacade = subEquipmentFacade;
    this.processFacade = processFacade;
  }

  /**
   * Creates a ControlTag in the server (cache and DB).
   * 
   * <p>Created ControlTags are not sent directly to the DAQ, but
   * only when a given Equipment points to them. If an Equipment
   * is not yet created, then the ControlTags are never sent
   * to the DAQ, which in any case needs restarting for the new
   * Equipment to be running. On the other hand, if an Equipment
   * already exists, the ControlTags will be forwarded to the DAQ
   * when the Equipment is updated to point to these new tags.
   * 
   * @param element contains the properties needed to create the control tag
   * @return change event requiring reboot but not sent to DAQ layer 
   * @throws IllegalAccessException if an error occurs when initializing the 
   *          HardwareAddress  
   */
  @Override
  @Transactional("cacheTransactionManager")
  public ProcessChange createControlTag(ConfigurationElement element) throws IllegalAccessException {
    LOGGER.trace("Creating ControlTag " + element.getEntityId());
    checkId(element.getEntityId());
    ControlTag controlTag = 
        commonTagFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
    try {
      configurableDAO.insert(controlTag);
      tagCache.putQuiet(controlTag);
      ProcessChange processChange = new ProcessChange();
      if (processFacade.getProcessFromControlTag(controlTag.getId()) != null) {
        processChange = new ProcessChange(processFacade.getProcessFromControlTag(controlTag.getId()));
      }
      return processChange;
    } catch (Exception e) {
      LOGGER.error("Exception caught while creating a ControlTag in cache - "
          + "rolling back DB transaction and removing from cache.", e);
      tagCache.remove(controlTag.getId());
      throw new RuntimeException(e);
    }
     
  }
  
  /**
   * If DB or cache update fails, rolls back DB transaction and removes Tag
   * from cache.
   */
  @Override
  @Transactional("cacheTransactionManager")
  public ProcessChange updateControlTag(Long id, Properties elementProperties) {
    LOGGER.trace("Updating ControlTag " + id);
    ControlTag controlTag = tagCache.get(id);
    Change controlTagUpdate;
    try {
      controlTag.getWriteLock().lock();
      configurableDAO.updateConfig(controlTag);
      controlTagUpdate = commonTagFacade.updateConfig(controlTag, elementProperties); //sets id of controlTagUpdate also     
      if (((ControlTagFacade) commonTagFacade).isInProcessList(controlTag)) {
        controlTag.getWriteLock().unlock();
        return getProcessChanges((DataTagUpdate) controlTagUpdate, id);  
      } else {        
        return new ProcessChange(); //no event for DAQ layer
      }           
    } catch (Exception ex) {
      LOGGER.error("Exception caught while updating a ControlTag - rolling back DB"
          + "transaction and removing Tag from cache.", ex);
      tagCache.remove(id);
      throw new UnexpectedRollbackException("Unexpected exception caught while updating a ControlTag configuration.", ex);
    } finally {
      if (controlTag.getWriteLock().isHeldByCurrentThread()) {
        controlTag.getWriteLock().unlock();
      }      
    }  
      
  }
  
  /**
   * Removes the control tag and fills in the passed report in case
   * of failure. When removing from DB and cache, unexpected exceptions
   * are caught and the tag is invalidated.
   * 
   * @param id the id of the tag to remove
   * @param tagReport the report for this removal
   * @return a DAQ configuration event if ControlTag is associated to some Equipment or SubEquipment and DAQ
   *          needs informing (i.e. if has Address) , else return null
   */
  @Override
  @Transactional("cacheTransactionManager")
  public ProcessChange removeControlTag(Long id, ConfigurationElementReport tagReport) {
    LOGGER.trace("Removing ControlTag " + id);
    try {
      ControlTag controlTag = tagCache.get(id);
      controlTag.getWriteLock().lock();
      try {        
        if (!controlTag.getRuleIds().isEmpty()) {
          LOGGER.trace("Removing rules dependent on ControlTag " + controlTag.getId());
          for (Long ruleId : controlTag.getRuleIds()) {
            ConfigurationElementReport newReport = new ConfigurationElementReport(Action.REMOVE, Entity.RULETAG, ruleId);
            tagReport.addSubReport(newReport);
            ruleTagConfigHandler.removeRuleTag(ruleId, newReport);
          }       
        } 
        if (!controlTag.getAlarmIds().isEmpty()) {
          LOGGER.trace("Removing Alarms dependent on ControlTag " + controlTag.getId());
          for (Long alarmId : new ArrayList<Long>(controlTag.getAlarmIds())) {
            ConfigurationElementReport alarmReport = new ConfigurationElementReport(Action.REMOVE, Entity.ALARM, alarmId);
            tagReport.addSubReport(alarmReport);
            alarmConfigHandler.removeAlarm(alarmId, alarmReport);
          } 
        } 
        //dataTagFacade.invalidate(controlTag, new DataTagQuality(DataTagQuality.REMOVED, "The ControlTag has been removed from the system and is no longer monitored."), new Timestamp(System.currentTimeMillis()));
        configurableDAO.deleteItem(controlTag.getId());
        tagCache.remove(controlTag.getId());    
        //if the ControlTag has no Address, do not send anything to the DAQ so return null
        if (((ControlTagFacade) commonTagFacade).isInProcessList(controlTag)) {
          controlTag.getWriteLock().unlock();
          //if the ControlTag is associated to some Equipment(or SubEquipment) inform the DAQ   
          DataTagRemove removeEvent = new DataTagRemove();
          removeEvent.setDataTagId(id);
          return getProcessChanges(removeEvent, id);        
        } else {
          return new ProcessChange();     
        }
      } catch (Exception ex) {
        //commonTagFacade.setStatus(controlTag, Status.RECONFIGURATION_ERROR);
        LOGGER.error("Exception caught while removing a control tag.", ex);
        tagReport.setFailure("Unable to remove ControlTag with id " + id); 
        throw new ConfigurationException(ConfigurationException.UNDEFINED, ex);
      } finally {
        if (controlTag.getWriteLock().isHeldByCurrentThread()) {
          controlTag.getWriteLock().unlock();
        }      
      } 
    } catch (CacheElementNotFoundException e) {
      LOGGER.warn("Attempting to remove a non-existent ControlTag - no action taken.");
      tagReport.setWarning("Attempting to removed a non-existent ControlTag");
      return new ProcessChange();
    }          
  }

  /**
   * Determines whether the ControlTag is associated to some
   * abstract equipment. If so, it constructs the required
   * ProcessChange list to send to the DAQ (note the ProcessChange
   * is always linked to the Equipment, not the SubEquipment which
   * has no associated ControlTags on the DAQ layer).
   * 
   * @param tagChange DAQ change event with Equipment id
   */
  private ProcessChange getProcessChanges(ITagChange tagChange, Long tagId) {              
    Map<Long, Long> equipmentControlTags = equipmentFacade.getAbstractEquipmentControlTags();
    Map<Long, Long> subEquipmentControlTags = subEquipmentFacade.getAbstractEquipmentControlTags(); 
    if (equipmentControlTags.containsKey(tagId)) {
      Long equipmentId = equipmentControlTags.get(tagId);
      tagChange.setEquipmentId(equipmentId);
      return new ProcessChange(equipmentFacade.getProcessForAbstractEquipment(equipmentId).getId(), tagChange);      
    } else if (subEquipmentControlTags.containsKey(tagId)) {
      Long subEquipmentId = subEquipmentControlTags.get(tagId);
      tagChange.setEquipmentId(subEquipmentFacade.getEquipmentForSubEquipment(subEquipmentId).getId());
      return new ProcessChange(subEquipmentFacade.getProcessForAbstractEquipment(subEquipmentId).getId(), tagChange);      
    } else {      
      return new ProcessChange();
    } 
  }
  
  /**
   * Given a ControlTag id, returns a create event for sending
   * to the DAQ layer if necessary. Returns null if no event needs
   * sending to the DAQ layer for this particular ControlTag.
   * 
   * <p>Currently, only alive tags with a DataTagAddress are sent
   * to the DAQ layer. All other cases only need an update to the
   * Equipment itself.
   * 
   * <p>Created ControlTags are only sent to the DAQ layer once they 
   * are referenced by some Equipment (and hence also belong to a given 
   * DAQ!). Updates to ControlTags can be sent immediately
   * 
   * @param configId the id of the configuration
   * @param controlTagId the id of the ControlTag that needs creating on the DAQ layer
   * @param equipmentId the id of the Equipment this control tag is attached to (compulsory)
   * @param processId the id of the Process to reconfigure
   * @return the change event including the process id
   */
  @Override
  public ProcessChange getCreateEvent(final Long configId, final Long controlTagId, final Long equipmentId, final Long processId) { 
    ControlTag controlTag = tagCache.get(controlTagId);
    controlTag.getWriteLock().lock();
    ProcessChange processChange = null;
    try {
      if (controlTag.getAddress() != null) {
         DataTagAdd dataTagAdd = new DataTagAdd(configId, equipmentId, 
                                                 dataTagFacade.generateSourceDataTag(controlTag));
         processChange = new ProcessChange(processId, dataTagAdd);
      }
      return processChange;
    } finally {
      controlTag.getWriteLock().unlock();
    }    
  }
 
}
