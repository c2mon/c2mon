/******************************************************************************
 * Copyright (C) 2010-2017 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.ControlTagLoaderDAO;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.configuration.config.ConfigurationProperties;
import cern.c2mon.server.common.listener.ConfigurationEventListener;
import cern.c2mon.server.configuration.config.ConfigurationProperties;
import cern.c2mon.server.configuration.handler.AlarmConfigHandler;
import cern.c2mon.server.configuration.handler.RuleTagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * See interface for doc.
 * 
 * @author Mark Brightwell
 *
 */
@Service
@Slf4j
public class ControlTagConfigTransactedImpl extends TagConfigTransactedImpl<ControlTag> implements ControlTagConfigTransacted {

  @Autowired
  private ConfigurationProperties properties;

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
                                        ControlTagFacade controlTagFacade,
                                        DataTagFacade dataTagFacade,
                                        EquipmentFacade equipmentFacade,
                                        ControlTagLoaderDAO controlTagLoaderDAO,
                                        TagLocationService tagLocationService,
                                        SubEquipmentFacade subEquipmentFacade,
                                        ProcessFacade processFacade,
                                        GenericApplicationContext context) {
    super(controlTagLoaderDAO, controlTagFacade, controlTagCache, tagLocationService, context);
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

    // TIMS-1048: Making check before write lock in order to avoid a deadlock situation
    checkId(element.getEntityId());
    
    tagCache.acquireWriteLockOnKey(element.getEntityId());
    try {
      log.trace("Creating ControlTag {}", element.getEntityId());
      ControlTag controlTag = commonTagFacade.createCacheObject(element.getEntityId(), element.getElementProperties());

      if (controlTag.getEquipmentId() != null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Equipment id cannot be set at creation time for ControlTags - unable to configure.");
      }

      try {
        configurableDAO.insert(controlTag);
      } catch (Exception e) {
        log.error("Exception caught while inserting a new Control Tag into the DB - rolling back changes", e);
        throw new UnexpectedRollbackException("Unexpected exception while creating a Control Tag: rolling back the change", e);
      }
      try {
        for (ConfigurationEventListener listener : configurationEventListeners) {
          listener.onConfigurationEvent(controlTag, Action.CREATE);
        }

        tagCache.putQuiet(controlTag);
        ProcessChange processChange = new ProcessChange();
        if (processFacade.getProcessIdFromControlTag(controlTag.getId()) != null) {
          processChange = new ProcessChange(processFacade.getProcessIdFromControlTag(controlTag.getId()));
        }
        return processChange;
      } catch (Exception e) {
        log.error("Exception caught while creating a ControlTag in cache - "
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
    log.trace("Updating ControlTag {}", id);
    Change controlTagUpdate;       
    tagCache.acquireWriteLockOnKey(id);
    try {      
      ControlTag controlTagCopy = tagCache.getCopy(id);

      // Removing temporally the equipment and process id to not store it into the database (chicken-egg problem)
      Long eqId = controlTagCopy.getEquipmentId();
      Long processId = controlTagCopy.getProcessId();

      ((ControlTagCacheObject) controlTagCopy).setEquipmentId(null);

      controlTagUpdate = commonTagFacade.updateConfig(controlTagCopy, elementProperties); //sets id of controlTagUpdate also
      configurableDAO.updateConfig(controlTagCopy);

      // Setting back equipment and process ID for cache object
      ((ControlTagCacheObject) controlTagCopy).setEquipmentId(eqId);
      ((ControlTagCacheObject) controlTagCopy).setProcessId(processId);

      for (ConfigurationEventListener listener : configurationEventListeners) {
        listener.onConfigurationEvent(controlTagCopy, Action.UPDATE);
      }

      tagCache.putQuiet(controlTagCopy);

      if (((ControlTagFacade) commonTagFacade).isInProcessList(controlTagCopy)) {
        tagCache.releaseWriteLockOnKey(id);
        return getProcessChanges((DataTagUpdate) controlTagUpdate, id);  
      } else {        
        return new ProcessChange(); //no event for DAQ layer
      }
    } catch (CacheElementNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("Exception caught while updating a ControlTag - rolling back DB transaction", ex);
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
    log.trace("Removing ControlTag {}", id);
    try {      
      if (this.properties.isDeleteRulesAfterTagDeletion()) {
        Collection<Long> ruleIds = tagCache.get(id).getCopyRuleIds();
        if (!ruleIds.isEmpty()) {
          log.trace("Removing rules dependent on ControlTag {}", id);
          for (Long ruleId : ruleIds) {
            ConfigurationElementReport newReport = new ConfigurationElementReport(Action.REMOVE, Entity.RULETAG, ruleId);
            tagReport.addSubReport(newReport);
            ruleTagConfigHandler.removeRuleTag(ruleId, newReport);
          }
        }
      }
      tagCache.acquireWriteLockOnKey(id);
      try {                
        ControlTag controlTag = tagCache.get(id);
        if (!controlTag.getAlarmIds().isEmpty()) {
          log.trace("Removing Alarms dependent on ControlTag " + controlTag.getId());
          for (Long alarmId : new ArrayList<Long>(controlTag.getAlarmIds())) {
            ConfigurationElementReport alarmReport = new ConfigurationElementReport(Action.REMOVE, Entity.ALARM, alarmId);
            tagReport.addSubReport(alarmReport);
            alarmConfigHandler.removeAlarm(alarmId, alarmReport);
          } 
        }

        for (ConfigurationEventListener listener : configurationEventListeners) {
          listener.onConfigurationEvent(controlTag, Action.REMOVE);
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
        log.error("Exception caught while removing a control tag.", ex);
        tagReport.setFailure("Unable to remove ControlTag with id " + id); 
        throw new UnexpectedRollbackException("Unable to remove control tag " + id, ex);
      } finally {
        if (tagCache.isWriteLockedByCurrentThread(id)) {
          tagCache.releaseWriteLockOnKey(id);
        }      
      } 
    } catch (CacheElementNotFoundException e) {
      log.warn("Attempting to remove a non-existent ControlTag - no action taken.");
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
