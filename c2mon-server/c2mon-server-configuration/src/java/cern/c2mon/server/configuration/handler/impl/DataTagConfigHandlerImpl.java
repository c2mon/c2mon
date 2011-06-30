/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.configuration.handler.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.configuration.handler.DataTagConfigHandler;
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
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.daq.config.Change;
import cern.tim.shared.daq.config.DataTagAdd;
import cern.tim.shared.daq.config.DataTagRemove;

/**
 * See interface documentation also.
 * 
 * <p>Currently all alarms and rules must be manually removed from any tag before it can be removed.
 * This also applied when removing an Equipment or Process: this will only succeed if all alarms
 * and rules have first been removed.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class DataTagConfigHandlerImpl extends TagConfigHandlerImpl<DataTag> implements DataTagConfigHandler  {
  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(DataTagConfigHandlerImpl.class);  
  
  /**
   * Reference to the equipment facade.
   */
  private EquipmentFacade equipmentFacade;  
  
  /**
   * Autowired constructor.
   * @param dataTagFacade reference to facade bean
   * @param dataTagLoaderDAO reference to DAO
   * @param dataTagCache reference to cache
   * @param equipmentFacade reference to equipment facade
   * @param tagLocationService reference to tag location bean
   */
  @Autowired
  public DataTagConfigHandlerImpl(final DataTagFacade dataTagFacade,
      final DataTagLoaderDAO dataTagLoaderDAO, final DataTagCache dataTagCache,
      final EquipmentFacade equipmentFacade, final TagLocationService tagLocationService) {
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
  public List<ProcessChange> createDataTag(final ConfigurationElement element) throws IllegalAccessException {    
    checkId(element.getEntityId());
    DataTag dataTag = (DataTag) commonTagFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
    try {
      configurableDAO.insert(dataTag);
      tagCache.putQuiet(dataTag);
      equipmentFacade.addTagToEquipment(dataTag.getEquipmentId(), dataTag.getId());
      DataTagAdd dataTagAdd = new DataTagAdd(element.getSequenceId(), dataTag.getEquipmentId(), 
                                      ((DataTagFacade) commonTagFacade).generateSourceDataTag(dataTag));
      ArrayList<ProcessChange> processChanges = new ArrayList<ProcessChange>();
      processChanges.add(new ProcessChange(equipmentFacade.getProcessForAbstractEquipment(dataTag.getEquipmentId()).getId(), dataTagAdd));    
      return processChanges;
    } catch (Exception ex) {
      LOGGER.error("Exception caught when attempting to create a DataTag - rolling back the DB transaction and undoing cache changes.");
      tagCache.remove(dataTag.getId());
      if (equipmentFacade.getDataTagIds(dataTag.getEquipmentId()).contains(dataTag.getId())) {
        equipmentFacade.removeTagFromEquipment(dataTag.getEquipmentId(), dataTag.getId());
      }      
      throw new RuntimeException(ex);
    }
        
  }
  
  /**
   * Updates the DataTag configuration in the cache and
   * database. 
   * 
   * <p>Throws an exception if an attempt is made to move
   * the tag to another Equipment: in this case the tag
   * should be removed and recreated.
   * @param id the id of the tag
   * @param properties the properties containing the changes
   * @return an change event if action is necessary by the DAQ; otherwise null
   */
  @Transactional(propagation = Propagation.REQUIRED) //("cacheTransactionManager")
  @Override
  public List<ProcessChange> updateDataTag(final Long id, final Properties properties) {
    //reject if trying to change equipment it is attached to - not currently allowed
    if (properties.containsKey("equipmentId")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, 
          "Attempting to change the equipment to which a tag is attached - this is not currently supported!");
    }
    Change dataTagUpdate = null;
    DataTag dataTag = tagCache.get(id);
    try {
      dataTag.getWriteLock().lock();
      dataTagUpdate = commonTagFacade.updateConfig(dataTag, properties); //TODO returns DAQ config report or null  
      configurableDAO.updateConfig(dataTag);      
      ArrayList<ProcessChange> processChanges = new ArrayList<ProcessChange>();
      processChanges.add(new ProcessChange(equipmentFacade.getProcessForAbstractEquipment(dataTag.getEquipmentId()).getId(), dataTagUpdate));
      return processChanges;
    } catch (Exception ex) {
      //((DataTagFacade) commonTagFacade).setStatus(dataTag, Status.RECONFIGURATION_ERROR);
      LOGGER.error("Exception caught while updating a datatag. Rolling back transaction and removing from cache.", ex);     
      tagCache.remove(id);
      throw new UnexpectedRollbackException("Unexpected exception caught while updating a DataTag configuration.", ex);
    } finally {
      dataTag.getWriteLock().unlock();
    }           
  }
  
  /**
   * If the tag has no associated alarms or rules, it is removed
   * from the database and cache. The reference to this tag in the
   * Equipment is also removed.
   * 
   * @param id the id of the DataTag to remove
   * @param elementReport is updated if removing is not possible
   * @return the DAQ change event, used if called directly from ConfigurationLoader 
   *      (not as consequence of Process removal for instance); IMPORTANT: config
   *      id of event still needs setting
   */
  @Transactional("cacheTransactionManager")
  @Override
  public List<ProcessChange> removeDataTag(final Long id, final ConfigurationElementReport elementReport) {  
    DataTag dataTag = tagCache.get(id);
    dataTag.getWriteLock().lock();
    try {     
      if (!dataTag.getRuleIds().isEmpty()) {
        String message = "Unable to remove DataTag with id " + id + " until the following rules have been removed " + dataTag.getRuleIds().toString(); 
        elementReport.setFailure(message);
        throw new RuntimeException(message);
      } else if (!dataTag.getAlarmIds().isEmpty()) {
        String message = "Unable to remove DataTag with id " + id + " until the following alarms have been removed " + dataTag.getAlarmIds().toString();
        elementReport.setFailure(message); 
        throw new RuntimeException(message);
      } else {
        //not possible below as removed from the cache by that point!!!
        //((DataTagFacade) commonTagFacade).invalidate(dataTag, new DataTagQuality(DataTagQuality.REMOVED, "The DataTag has been removed from the system and is no longer monitored."), new Timestamp(System.currentTimeMillis()));
        configurableDAO.deleteItem(dataTag.getId());
        tagCache.remove(dataTag.getId());        
      }
      dataTag.getWriteLock().unlock();
      //outside above lock as locks equipment (lock hierarchy: never lock equipment after tag)
      try {
        equipmentFacade.removeTagFromEquipment(dataTag.getEquipmentId(), dataTag.getId());
      } catch (CacheElementNotFoundException cacheEx) {
        LOGGER.warn("Unable to locate Equipment with id " + dataTag.getEquipmentId() + "in the cache, when attempting to remove a Tag reference from it.");
      }      
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
    ArrayList<ProcessChange> processChanges = new ArrayList<ProcessChange>();
    processChanges.add(new ProcessChange(equipmentFacade.getProcessForAbstractEquipment(dataTag.getEquipmentId()).getId(), removeEvent));
    return processChanges;
  }
  
  
}
