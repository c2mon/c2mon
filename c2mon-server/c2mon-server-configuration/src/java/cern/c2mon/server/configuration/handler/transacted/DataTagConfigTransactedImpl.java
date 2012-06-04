package cern.c2mon.server.configuration.handler.transacted;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.configuration.handler.AlarmConfigHandler;
import cern.c2mon.server.configuration.handler.RuleTagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.cache.DataTagCache;
import cern.tim.server.cache.DataTagFacade;
import cern.tim.server.cache.EquipmentFacade;
import cern.tim.server.cache.TagLocationService;
import cern.tim.server.cache.exception.CacheElementNotFoundException;
import cern.tim.server.cache.loading.DataTagLoaderDAO;
import cern.tim.server.common.datatag.DataTag;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.client.configuration.ConfigConstants.Action;
import cern.tim.shared.client.configuration.ConfigConstants.Entity;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.daq.config.Change;
import cern.tim.shared.daq.config.DataTagAdd;
import cern.tim.shared.daq.config.DataTagRemove;

/**
 * Implementation of transacted methods.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class DataTagConfigTransactedImpl extends TagConfigTransactedImpl<DataTag> implements DataTagConfigTransacted {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(DataTagConfigTransactedImpl.class);  
  
  /**
   * Reference to the equipment facade.
   */
  private EquipmentFacade equipmentFacade; 
  
  /**
   * For recursive deletion of rules.
   */
  @Autowired
  private RuleTagConfigHandler ruleTagConfigHandler;
  
  /**
   * For recursive deletion of alarms.
   */
  @Autowired
  private AlarmConfigHandler alarmConfigHandler;
  
  /**
   * Autowired constructor.
   * @param dataTagFacade reference to facade bean
   * @param dataTagLoaderDAO reference to DAO
   * @param dataTagCache reference to cache
   * @param equipmentFacade reference to equipment facade
   * @param tagLocationService reference to tag location bean
   */
  @Autowired
  public DataTagConfigTransactedImpl(final DataTagFacade dataTagFacade,
      final DataTagLoaderDAO dataTagLoaderDAO, final DataTagCache dataTagCache,
      final EquipmentFacade equipmentFacade, final TagLocationService tagLocationService){
    super(dataTagLoaderDAO, dataTagFacade, dataTagCache, tagLocationService);           
    this.equipmentFacade = equipmentFacade;       
  }
  
  /**
   * Create the cache objects, puts it in the DB, loads it into the cache, and returns the 
   * change event for sending to the DAQ.
   * @param element the server configuration element
   * @return the change event to send to the DAQ
   * @throws IllegalAccessException 
   * @throws RuntimeException if any error occurs during reconfiguration; DB transaction is rolled back and cache elements are removed
   */
  @Transactional("cacheTransactionManager")
  @Override
  public ProcessChange doCreateDataTag(final ConfigurationElement element) throws IllegalAccessException {
    LOGGER.trace("Creating DataTag " + element.getEntityId());
    checkId(element.getEntityId());
    DataTag dataTag = (DataTag) commonTagFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
    try {
      configurableDAO.insert(dataTag);
    } catch (Exception e) {
      LOGGER.error("Exception caught while inserting a new DataTag into the DB - rolling back changes", e);
      throw new UnexpectedRollbackException("Unexpected exception while creating a DataTag: rolling back the change", e);
    }
    try {
      tagCache.putQuiet(dataTag);
      equipmentFacade.addTagToEquipment(dataTag.getEquipmentId(), dataTag.getId());
      DataTagAdd dataTagAdd = new DataTagAdd(element.getSequenceId(), dataTag.getEquipmentId(), 
                                      ((DataTagFacade) commonTagFacade).generateSourceDataTag(dataTag));      
      return new ProcessChange(equipmentFacade.getProcessForAbstractEquipment(dataTag.getEquipmentId()).getId(), dataTagAdd);          
    } catch (Exception ex) {
      LOGGER.error("Exception caught when attempting to create a DataTag - rolling back the DB transaction and undoing cache changes.");
      tagCache.remove(dataTag.getId());
      if (equipmentFacade.getDataTagIds(dataTag.getEquipmentId()).contains(dataTag.getId())) {
        equipmentFacade.removeTagFromEquipment(dataTag.getEquipmentId(), dataTag.getId());
      }      
      throw new UnexpectedRollbackException("Unexpected exception while creating a DataTag: rolling back the change", ex);
    }
        
  }
  
  /**
   * Updates the DataTag configuration in the cache and
   * database. 
   * 
   * <p>Throws an exception if an attempt is made to move
   * the tag to another Equipment: in this case the tag
   * should be removed and recreated from the DB: for this
   * reason this call requires a NEW TRANSACTION, so the calling
   * method can reload the object from a rolled back DB.
   * 
   * @param id the id of the tag
   * @param properties the properties containing the changes
   * @return an change event if action is necessary by the DAQ; otherwise null
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW) //("cacheTransactionManager")
  @Override
  public ProcessChange doUpdateDataTag(final Long id, final Properties properties) {
    LOGGER.trace("Updating DataTag " + id);
    //reject if trying to change equipment it is attached to - not currently allowed
    if (properties.containsKey("equipmentId")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, 
          "Attempting to change the equipment to which a tag is attached - this is not currently supported!");
    }
    Change dataTagUpdate = null;
    DataTag dataTag = tagCache.get(id);
    try {
      dataTag.getWriteLock().lock();
      dataTagUpdate = commonTagFacade.updateConfig(dataTag, properties);
      configurableDAO.updateConfig(dataTag);            
      return new ProcessChange(equipmentFacade.getProcessForAbstractEquipment(dataTag.getEquipmentId()).getId(), dataTagUpdate);      
    } catch (Exception ex) {
      LOGGER.error("Exception caught while updating a datatag. Rolling back transaction and removing from cache.", ex);           
      throw new UnexpectedRollbackException("Unexpected exception caught while updating a DataTag configuration.", ex);
    } finally {
      dataTag.getWriteLock().unlock();
    }           
  }
  
  @Override
  @Transactional(value = "cacheTransactionManager", propagation=Propagation.REQUIRES_NEW,isolation = Isolation.READ_COMMITTED)
  public List<ProcessChange> doRemoveDataTag(final Long id, final ConfigurationElementReport elementReport) {
    LOGGER.trace("Removing DataTag " + id);
    ArrayList<ProcessChange> processChanges = new ArrayList<ProcessChange>();
    try {
      DataTag dataTag = tagCache.get(id);
      Collection<Long> ruleIds = dataTag.getCopyRuleIds();  
      if (!ruleIds.isEmpty()) {
        LOGGER.trace("Removing Rules dependent on DataTag " + id);
        for (Long ruleId : new ArrayList<Long>(ruleIds)) {
          if (tagLocationService.isInTagCache(ruleId)) { //may already have been removed if a previous rule in the list was used in this rule! {
            ConfigurationElementReport newReport = new ConfigurationElementReport(Action.REMOVE, Entity.RULETAG, ruleId);
            elementReport.addSubReport(newReport);
            ruleTagConfigHandler.removeRuleTag(ruleId, newReport); 
          }          
        }
      }
      dataTag.getWriteLock().lock();
      try {                
        Collection<Long> alarmIds = dataTag.getCopyAlarmIds();
        if (!alarmIds.isEmpty()) {
          LOGGER.trace("Removing Alarms dependent on DataTag " + id);
          for (Long alarmId : new ArrayList<Long>(alarmIds)) {
            ConfigurationElementReport alarmReport = new ConfigurationElementReport(Action.REMOVE, Entity.ALARM, alarmId);
            elementReport.addSubReport(alarmReport);
            alarmConfigHandler.removeAlarm(alarmId, alarmReport);
          }        
        }
        configurableDAO.deleteItem(dataTag.getId());                
      } catch (Exception ex) {
        //commonTagFacade.setStatus(dataTag, Status.RECONFIGURATION_ERROR);
        elementReport.setFailure("Exception caught while removing datatag", ex);
        LOGGER.error("Exception caught while removing datatag with id " + id + "; rolling back DB transaction.", ex);
        throw new UnexpectedRollbackException("Exception caught while removing datatag.", ex);      
      } finally {
        if (dataTag.getWriteLock().isHeldByCurrentThread()) {
          dataTag.getWriteLock().unlock();
        }      
      }
      //if successful so far add remove event for DAQ layer
      DataTagRemove removeEvent = new DataTagRemove();  
      removeEvent.setDataTagId(id);
      removeEvent.setEquipmentId(dataTag.getEquipmentId());      
      processChanges.add(new ProcessChange(equipmentFacade.getProcessForAbstractEquipment(dataTag.getEquipmentId()).getId(), removeEvent));
    } catch (CacheElementNotFoundException e) {
      LOGGER.warn("Attempting to remove a non-existent DataTag - no action taken.");
      elementReport.setWarning("Attempting to remove a non-existent DataTag");
    }    
    return processChanges;
  }
  
}
