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
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.DataTagConfigHandler;
import cern.c2mon.server.configuration.handler.EquipmentConfigHandler;
import cern.c2mon.server.configuration.handler.ProcessConfigHandler;
import cern.c2mon.server.configuration.handler.SubEquipmentConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.cache.AliveTimerCache;
import cern.tim.server.cache.CommFaultTagCache;
import cern.tim.server.cache.EquipmentCache;
import cern.tim.server.cache.EquipmentFacade;
import cern.tim.server.cache.ProcessCache;
import cern.tim.server.cache.loading.EquipmentDAO;
import cern.tim.server.common.equipment.Equipment;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.client.configuration.ConfigConstants.Action;
import cern.tim.shared.client.configuration.ConfigConstants.Entity;
import cern.tim.shared.common.ConfigurationException;

/**
 * See interface documentation.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class EquipmentConfigHandlerImpl extends AbstractEquipmentConfigHandler<Equipment> implements EquipmentConfigHandler {

  private static final Logger LOGGER = Logger.getLogger(EquipmentConfigHandlerImpl.class); 
  
  private EquipmentFacade equipmentFacade;
  
  private EquipmentDAO equipmentDAO;
  
  private EquipmentCache equipmentCache;
  
  private DataTagConfigHandler dataTagConfigHandler; 
  
  private CommandTagConfigHandler commandTagConfigHandler;
  
  private SubEquipmentConfigHandler subEquipmentConfigHandler;
  
  private ProcessCache processCache;
  
  @Autowired
  private ProcessConfigHandler processConfigHandler;
  
  @Autowired
  public EquipmentConfigHandlerImpl(ControlTagConfigHandler controlTagConfigHandler, AliveTimerCache aliveTimerCache,
                                CommFaultTagCache commFaultTagCache, EquipmentCache abstractEquipmentCache, 
                                EquipmentFacade equipmentFacade, EquipmentDAO equipmentDAO, EquipmentCache equipmentCache,
                                DataTagConfigHandler dataTagConfigHandler, CommandTagConfigHandler commandTagConfigHandler, 
                                SubEquipmentConfigHandler subEquipmentConfigHandler, ProcessCache processCache) {
    super(controlTagConfigHandler, equipmentFacade, abstractEquipmentCache, equipmentDAO,
        aliveTimerCache, commFaultTagCache);
    this.equipmentFacade = equipmentFacade;
    this.equipmentDAO = equipmentDAO;
    this.equipmentCache = equipmentCache;
    this.dataTagConfigHandler = dataTagConfigHandler;       
    this.commandTagConfigHandler = commandTagConfigHandler;
    this.subEquipmentConfigHandler = subEquipmentConfigHandler;
    this.processCache = processCache;
  }

  /**
   * Inserts the equipment into the cache and updates the DB.
   * The Process in the cache is updated to refer to the new
   * Equipment.
   * 
   * <p>Also updates the associated cache object in the AliveTimer
   * and CommFaultTag caches. 
   * 
   * @param element the configuration element
   * @throws IllegalAccessException 
   */
  @Override
  public ProcessChange createEquipment(ConfigurationElement element) throws IllegalAccessException {
    Equipment equipment = super.createAbstractEquipment(element);
    equipmentFacade.addEquipmentToProcess(equipment.getId(), equipment.getProcessId());
    return new ProcessChange(equipment.getProcessId());
  }
  
  @Override
  @Transactional("cacheTransactionManager")
  public List<ProcessChange> updateEquipment(Long equipmentId, Properties properties) throws IllegalAccessException {
    if (properties.containsKey("processId")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, 
          "Attempting to change the parent process id of an equipment - this is not currently supported!");
    }
    Equipment equipment = equipmentCache.get(equipmentId);
    equipment.getWriteLock().lock();
    try {        
      return super.updateAbstractEquipment(equipment, properties);
    } catch (UnexpectedRollbackException e) {
      processCache.remove(equipment.getProcessId());
      throw e;
    } finally {
      equipment.getWriteLock().unlock();
    }
  }
  
  /**
   * Rules need to be removed before this operation can complete successfully
   * (DataTags on which rules depend will be left in place, consistently in cache
   * and DB; DAQ will continue to function as expected, but with the RECONFIGURED
   * process state).
   * 
   * <p>In general, if a DataTag cannot be removed, the operation will keep Equipment
   * SubEquipment and Processes in place (including ControlTags).
   *
   * <p>Any failure when removing the SubEquipments will interrupt the Equipment removal
   * and leave it in the current state (no rollback of SubEquipment removals).
   * 
   * <p>Remove operations should always succeed as a last resort to restoring a consistent
   * configuration. Will only fail to complete if alarms or rules are associated to tags
   * managed by this equipment. In this case these should be removed first.
   * 
   * 
   * @param equipmentid the id of the equipment to be removed
   * @param equipmentReport the equipment-level configuration report
   * @return always returns a change object requiring restart (remove not supported on DAQ layer so far)
   */
  @Override
  @Transactional("cacheTransactionManager")
  public ProcessChange removeEquipment(final Long equipmentid, final ConfigurationElementReport equipmentReport) {
    LOGGER.debug("Removing Equipment " + equipmentid);
    if (equipmentCache.hasKey(equipmentid)) {
      Equipment equipment = equipmentCache.get(equipmentid);    
      try {
        //remove alive timers and commfault from cache first, before locking! (lock hierarchy)
        equipmentFacade.removeAliveTimer(equipmentid);
        equipmentFacade.removeCommFault(equipmentid);
        equipment.getWriteLock().lock();
        removeEquipmentTags(equipment, equipmentReport);
        removeEquipmentCommands(equipment, equipmentReport);
        removeSubEquipments(equipment, equipmentReport);
        equipmentDAO.deleteItem(equipmentid);
        equipmentCache.remove(equipmentid);
        removeEquipmentControlTags(equipment, equipmentReport); //must be removed last as equipment references them
        equipment.getWriteLock().unlock();           
        processConfigHandler.removeEquipmentFromProcess(equipmentid, equipment.getProcessId());
        return new ProcessChange(equipment.getProcessId());
      } catch (UnexpectedRollbackException ex) {
        equipmentReport.setFailure("Aborting removal of equipment "
            + equipmentid + " as unable to remove all associated datatags."); 
        throw new UnexpectedRollbackException("Aborting removal of Equipment as failed to remove all" 
            + "associated datatags and commandtags.", ex);
      } finally {
        if (equipment.getWriteLock().isHeldByCurrentThread()) {
          equipment.getWriteLock().unlock();
        }      
      }
    } else {
      LOGGER.debug("Equipment not found in cache - unable to remove it.");
      equipmentReport.setWarning("Equipment not found in cache so cannot be removed.");
      return new ProcessChange();
    }
             
  }

  

  /**
   * Removes the subequipments attached to this equipment.
   * Exceptions are caught, added to the report and thrown
   * up to interrupt the equipment removal.
   * 
   *<p>Call within Equipment lock.
   * 
   * @param equipment the equipment for which the subequipments should be removed
   * @param equipmentReport the report at the equipment level
   */
  private void removeSubEquipments(Equipment equipment, ConfigurationElementReport equipmentReport) {
    for (Long subEquipmentId : new ArrayList<Long>(equipment.getSubEquipmentIds())) {
      ConfigurationElementReport subEquipmentReport = new ConfigurationElementReport(Action.REMOVE, Entity.SUBEQUIPMENT, subEquipmentId);
      equipmentReport.addSubReport(subEquipmentReport);
      try {
        subEquipmentConfigHandler.removeSubEquipment(subEquipmentId, subEquipmentReport);
      } catch (Exception ex) {
        subEquipmentReport.setFailure("Exception caught - aborting removal of subequipment "
            + subEquipmentId , ex); 
        throw new RuntimeException("Aborting reconfiguration as unable to remove subequipment.", ex);
      }
      
    }
  }

  /**
   * Removes all command tags associated with this equipment.
   * @param equipment
   * @param equipmentReport
   * @return
   */
  private void removeEquipmentCommands(Equipment equipment, ConfigurationElementReport equipmentReport) {
   
    for (Long commandTagId : new ArrayList<Long>(equipment.getCommandTagIds())) { //copy as modified when removing command tag
      ConfigurationElementReport commandReport = new ConfigurationElementReport(Action.REMOVE, Entity.COMMANDTAG, commandTagId);
      equipmentReport.addSubReport(commandReport);
      commandTagConfigHandler.removeCommandTag(commandTagId, commandReport);         
    }

  }

  /**
   * Removes the tags for this equipment. The DAQ is not informed as
   * this method is only called when the whole Equipment is removed.
   * 
   * <p>Call within equipment lock.
   * @param equipment for which the tags should be removed
   * @throws RuntimeException if fail to remove tag
   */
  private void removeEquipmentTags(Equipment equipment, ConfigurationElementReport equipmentReport) {   
    for (Long dataTagId : new ArrayList<Long>(equipment.getDataTagIds())) { //copy as list is modified by removeDataTag
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.DATATAG, dataTagId);
      equipmentReport.addSubReport(tagReport);
      dataTagConfigHandler.removeDataTag(dataTagId, tagReport);
    }   
  }
  
  /**
   * Adds a tag to the equipment.
   * @param equipmentId
   * @param dataTagId
   */
//  public void addDataTagToEquipment(Long equipmentId, Long dataTagId) {
//    equipmentFacade.addTagToEquipment(equipmentId, dataTagId);        
//  }
  
  /**
   * Removes the specified tag from the list of tags for this equipment.
   * @param equipmentId
   * @param dataTagId
   */
//  public void removeDataTagFromEquipment(Long equipmentId, Long dataTagId) {    
//    equipmentFacade.removeTagFromEquipment(equipmentId, dataTagId);              
//  }

  
}
