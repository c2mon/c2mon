package cern.c2mon.server.configuration.handler;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.cache.ControlTagCache;
import cern.tim.server.cache.ControlTagFacade;
import cern.tim.server.cache.DataTagFacade;
import cern.tim.server.cache.EquipmentFacade;
import cern.tim.server.cache.SubEquipmentFacade;
import cern.tim.server.cache.TagLocationService;
import cern.tim.server.cache.loading.ControlTagLoaderDAO;
import cern.tim.server.common.control.ControlTag;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.common.datatag.DataTagQuality;
import cern.tim.shared.daq.config.Change;
import cern.tim.shared.daq.config.DataTagAdd;
import cern.tim.shared.daq.config.DataTagRemove;
import cern.tim.shared.daq.config.DataTagUpdate;
import cern.tim.shared.daq.config.ITagChange;

/**
 * Manages reconfigurations of control tags.
 *  
 * <p>TODO update and remove are same as {@link DataTagConfigHandler} so could extend it
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class ControlTagConfigHandler extends TagConfigHandlerImpl<ControlTag> {
  
  private static final Logger LOGGER = Logger.getLogger(ControlTagConfigHandler.class);
 
  private DataTagFacade dataTagFacade;
  
  private EquipmentFacade equipmentFacade;
  
  private SubEquipmentFacade subEquipmentFacade; 
  
  
  @Autowired
  public ControlTagConfigHandler(ControlTagCache controlTagCache,
      ControlTagFacade controlTagFacade, DataTagFacade dataTagFacade,
      EquipmentFacade equipmentFacade,
      ControlTagLoaderDAO controlTagLoaderDAO, TagLocationService tagLocationService, SubEquipmentFacade subEquipmentFacade) {
    super(controlTagLoaderDAO, controlTagFacade, controlTagCache, tagLocationService);    
    this.dataTagFacade = dataTagFacade;
    this.equipmentFacade = equipmentFacade; 
    this.subEquipmentFacade = subEquipmentFacade;
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
   * @throws IllegalAccessException if an error occurs when initializing the 
   *          HardwareAddress  
   */
  public void createControlTag(ConfigurationElement element) throws IllegalAccessException {
    checkId(element.getEntityId());
    ControlTag controlTag = 
        commonTagFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
    configurableDAO.insert(controlTag);
    tagCache.putQuiet(controlTag);    
  }
  
  public List<ProcessChange> updateControlTag(Long id, Properties elementProperties) {
    ControlTag controlTag = tagCache.get(id);
    Change controlTagUpdate;
    try {
      controlTag.getWriteLock().lock();
      controlTagUpdate = commonTagFacade.updateConfig(controlTag, elementProperties); //sets id of controlTagUpdate also
      configurableDAO.updateConfig(controlTag);
      if (((ControlTagFacade) commonTagFacade).isInProcessList(controlTag)) {
        controlTag.getWriteLock().unlock();
        return getProcessChanges((DataTagUpdate) controlTagUpdate, id);  
      } else {
        return null;
      }     
    } catch (Exception ex) {
      //dataTagFacade.setStatus(controlTag, Status.RECONFIGURATION_ERROR);
      LOGGER.error("Exception caught while updating a datatag.", ex);
      throw new RuntimeException(ex);
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
  public List<ProcessChange> removeControlTag(Long id, ConfigurationElementReport tagReport) {
    ControlTag controlTag = tagCache.get(id);
    try {
      controlTag.getWriteLock().lock();
      if (!controlTag.getRuleIds().isEmpty()) {
        tagReport.setFailure("Unable to remove ControlTag with id " + id + " until the following rules have been removed " + controlTag.getRuleIds().toString());      
      } else if (!controlTag.getAlarmIds().isEmpty()) {
        tagReport.setFailure("Unable to remove ControlTag with id " + id + " until the following alarms have been removed " + controlTag.getAlarmIds().toString()); 
      } else {
        //dataTagFacade.invalidate(controlTag, new DataTagQuality(DataTagQuality.REMOVED, "The ControlTag has been removed from the system and is no longer monitored."), new Timestamp(System.currentTimeMillis()));
        configurableDAO.deleteItem(controlTag.getId());
        tagCache.remove(controlTag.getId());
      }
      //if the ControlTag has no Address, do not send anything to the DAQ so return null
      if (((ControlTagFacade) commonTagFacade).isInProcessList(controlTag)) {
        controlTag.getWriteLock().unlock();
        //if the ControlTag is associated to some Equipment(or SubEquipment) inform the DAQ   
        DataTagRemove removeEvent = new DataTagRemove();
        removeEvent.setDataTagId(id);
        return getProcessChanges(removeEvent, id);        
      } else {
        return null;     
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
  }

  /**
   * Determines whether the ControlTag is associated to some
   * abstract equipment. If so, it constructs the required
   * ProcessChange list to send to the DAQ (note the ProcessChange
   * is always linked to the Equipment, not the SubEquipment which
   * has no associated ControlTags on the DAQ layer).
   * 
   * @param tagChange DAQ change event with Equipment id 
   * @return returns null if ControlTag is not associated to a (Sub)Equipment
   */
  private List<ProcessChange> getProcessChanges(ITagChange tagChange, Long tagId) {          
    ArrayList<ProcessChange> processChanges  = new ArrayList<ProcessChange>();
    Map<Long, Long> equipmentControlTags = equipmentFacade.getAbstractEquipmentControlTags();
    Map<Long, Long> subEquipmentControlTags = subEquipmentFacade.getAbstractEquipmentControlTags(); 
    if (equipmentControlTags.containsKey(tagId)) {
      Long equipmentId = equipmentControlTags.get(tagId);
      tagChange.setEquipmentId(equipmentId);
      processChanges.add(new ProcessChange(equipmentFacade.getProcessForAbstractEquipment(equipmentId).getId(), tagChange));
      return processChanges;
    } else if (subEquipmentControlTags.containsKey(tagId)) {
      Long subEquipmentId = subEquipmentControlTags.get(tagId);
      tagChange.setEquipmentId(subEquipmentFacade.getEquipmentForSubEquipment(subEquipmentId).getId());
      processChanges.add(new ProcessChange(subEquipmentFacade.getProcessForAbstractEquipment(subEquipmentId).getId(), tagChange));
      return processChanges;
    } else {
      return null;
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
   * DAQ!). Updates to ControlTags can be send immediately
   * 
   * @param configId the id of the configuration
   * @param controlTagId the id of the ControlTag that needs creating on the DAQ layer
   * @param equipmentId the id of the Equipment this control tag is attached to (compulsory)
   * @param processId the id of the Process to reconfigure
   * @return the change event including the process id
   */
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
