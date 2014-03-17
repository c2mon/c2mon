package cern.c2mon.server.configuration.handler.transacted;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.configuration.handler.AlarmConfigHandler;
import cern.c2mon.server.configuration.handler.RuleTagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.ControlTagFacade;
import cern.c2mon.server.cache.DataTagFacade;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.ProcessFacade;
import cern.c2mon.server.cache.SubEquipmentFacade;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.ControlTagLoaderDAO;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.DataTagAdd;
import cern.c2mon.shared.daq.config.DataTagRemove;
import cern.c2mon.shared.daq.config.DataTagUpdate;
import cern.c2mon.shared.daq.config.ITagChange;

/**
 * See interface for doc.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class ControlTagConfigTransactedImpl extends TagConfigTransactedImpl<ControlTag> implements ControlTagConfigTransacted {

  private static final Logger LOGGER = Logger.getLogger(ControlTagConfigTransactedImpl.class);
  
  private DataTagFacade dataTagFacade;
  
  private EquipmentFacade equipmentFacade;
  
  private SubEquipmentFacade subEquipmentFacade; 
  
  private ProcessFacade processFacade;
  
  @Autowired
  private RuleTagConfigHandler ruleTagConfigHandler;
  
  @Autowired
  private AlarmConfigHandler alarmConfigHandler;
  
  
  @Autowired
  public ControlTagConfigTransactedImpl(ControlTagCache controlTagCache,
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
  @Transactional(value = "cacheTransactionManager")
  public ProcessChange doCreateControlTag(ConfigurationElement element) throws IllegalAccessException {
    tagCache.acquireWriteLockOnKey(element.getEntityId());
    try {
      LOGGER.trace("Creating ControlTag " + element.getEntityId());
      checkId(element.getEntityId());
      ControlTag controlTag = commonTagFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
      try {
        configurableDAO.insert(controlTag);
      } catch (Exception e) {
        LOGGER.error("Exception caught while inserting a new Control Tag into the DB - rolling back changes", e);
        throw new UnexpectedRollbackException("Unexpected exception while creating a Control Tag: rolling back the change", e);
      }
      try {
        tagCache.putQuiet(controlTag);      
        ProcessChange processChange = new ProcessChange();
        if (processFacade.getProcessIdFromControlTag(controlTag.getId()) != null) {
          processChange = new ProcessChange(processFacade.getProcessIdFromControlTag(controlTag.getId()));
        }
        return processChange;
      } catch (Exception e) {
        LOGGER.error("Exception caught while creating a ControlTag in cache - "
            + "rolling back DB transaction and removing from cache.", e);
        tagCache.remove(controlTag.getId());
        throw new UnexpectedRollbackException("Unexpected exception while creating a Control Tag: rolling back the change", e);
      }
    } finally {
      tagCache.releaseWriteLockOnKey(element.getEntityId());
    }
     
  }
  
  /**
   * If DB or cache update fails, rolls back DB transaction and removes Tag
   * from cache.
   */
  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public ProcessChange doUpdateControlTag(Long id, Properties elementProperties) {
    LOGGER.trace("Updating ControlTag " + id);
    Change controlTagUpdate;       
    tagCache.acquireWriteLockOnKey(id);
    try {      
      ControlTag controlTag = tagCache.get(id);
      configurableDAO.updateConfig(controlTag);
      controlTagUpdate = commonTagFacade.updateConfig(controlTag, elementProperties); //sets id of controlTagUpdate also     
      if (((ControlTagFacade) commonTagFacade).isInProcessList(controlTag)) {
        tagCache.releaseWriteLockOnKey(id);
        return getProcessChanges((DataTagUpdate) controlTagUpdate, id);  
      } else {        
        return new ProcessChange(); //no event for DAQ layer
      }
    } catch (CacheElementNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      LOGGER.error("Exception caught while updating a ControlTag - rolling back DB transaction", ex);      
      throw new UnexpectedRollbackException("Unexpected exception caught while updating a ControlTag configuration", ex);
    } finally {
      if (tagCache.isWriteLockedByCurrentThread(id)) {
        tagCache.releaseWriteLockOnKey(id);
      }      
    }  
      
  }    
  
  @Override
  @Transactional(value = "cacheTransactionManager", propagation=Propagation.REQUIRES_NEW)
  public ProcessChange doRemoveControlTag(Long id, ConfigurationElementReport tagReport) {
    LOGGER.trace("Removing ControlTag " + id);
    try {      
      Collection<Long> ruleIds = tagCache.get(id).getCopyRuleIds();
      if (!ruleIds.isEmpty()) {
        LOGGER.trace("Removing rules dependent on ControlTag " + id);
        for (Long ruleId : ruleIds) {
          ConfigurationElementReport newReport = new ConfigurationElementReport(Action.REMOVE, Entity.RULETAG, ruleId);
          tagReport.addSubReport(newReport);
          ruleTagConfigHandler.removeRuleTag(ruleId, newReport);
        }       
      }
      tagCache.acquireWriteLockOnKey(id);      
      try {                
        ControlTag controlTag = tagCache.get(id);
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
        //if the ControlTag has no Address, do not send anything to the DAQ so return null
        if (((ControlTagFacade) commonTagFacade).isInProcessList(controlTag)) {
          tagCache.releaseWriteLockOnKey(id);
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
        throw new UnexpectedRollbackException("Unable to remove control tag " + id, ex);
      } finally {
        if (tagCache.isWriteLockedByCurrentThread(id)) {
          tagCache.releaseWriteLockOnKey(id);
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
      return new ProcessChange(equipmentFacade.getProcessIdForAbstractEquipment(equipmentId), tagChange);      
    } else if (subEquipmentControlTags.containsKey(tagId)) {
      Long subEquipmentId = subEquipmentControlTags.get(tagId);
      tagChange.setEquipmentId(subEquipmentFacade.getEquipmentIdForSubEquipment(subEquipmentId));
      return new ProcessChange(subEquipmentFacade.getProcessIdForAbstractEquipment(subEquipmentId), tagChange);      
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
    tagCache.acquireWriteLockOnKey(controlTagId);    
    ProcessChange processChange = null;
    try {
      ControlTag controlTag = tagCache.get(controlTagId);
      if (controlTag.getAddress() != null) {
         DataTagAdd dataTagAdd = new DataTagAdd(configId, equipmentId, 
                                                 dataTagFacade.generateSourceDataTag(controlTag));
         processChange = new ProcessChange(processId, dataTagAdd);
      }
      return processChange;
    } finally {
      tagCache.releaseWriteLockOnKey(controlTagId);
    }    
  } 
  
}
