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
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.DataTagConfigHandler;
import cern.c2mon.server.configuration.handler.EquipmentConfigHandler;
import cern.c2mon.server.configuration.handler.ProcessConfigHandler;
import cern.c2mon.server.configuration.handler.SubEquipmentConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.EquipmentConfigTransacted;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.cache.AliveTimerCache;
import cern.tim.server.cache.CommFaultTagCache;
import cern.tim.server.cache.EquipmentCache;
import cern.tim.server.cache.EquipmentFacade;
import cern.tim.server.cache.exception.CacheElementNotFoundException;
import cern.tim.server.common.equipment.Equipment;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.client.configuration.ConfigConstants.Action;
import cern.tim.shared.client.configuration.ConfigConstants.Entity;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.daq.config.EquipmentUnitRemove;

/**
 * See interface documentation.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class EquipmentConfigHandlerImpl extends AbstractEquipmentConfigHandler<Equipment> implements EquipmentConfigHandler {
  
  private static final Logger LOGGER = Logger.getLogger(EquipmentConfigHandlerImpl.class);
  
  private EquipmentConfigTransacted equipmentConfigTransacted;
  
  private SubEquipmentConfigHandler subEquipmentConfigHandler;
  
  private DataTagConfigHandler dataTagConfigHandler;
  
  private CommandTagConfigHandler commandTagConfigHandler;
  
  private EquipmentFacade equipmentFacade;
  
  @Autowired  
  private ProcessConfigHandler processConfigHandler;
  
  private EquipmentCache equipmentCache;
  
  /**
   * Autowired constructor.
   */
  @Autowired
  public EquipmentConfigHandlerImpl(SubEquipmentConfigHandler subEquipmentConfigHandler, DataTagConfigHandler dataTagConfigHandler,
      CommandTagConfigHandler commandTagConfigHandler, EquipmentFacade equipmentFacade, EquipmentCache equipmentCache,
      ControlTagConfigHandler controlTagConfigHandler, EquipmentConfigTransacted equipmentConfigTransacted,
      AliveTimerCache aliveTimerCache, CommFaultTagCache commFaultTagCache) {
    super(controlTagConfigHandler, equipmentConfigTransacted, equipmentCache, aliveTimerCache, commFaultTagCache, equipmentFacade);
    this.subEquipmentConfigHandler = subEquipmentConfigHandler;
    this.dataTagConfigHandler = dataTagConfigHandler;
    this.commandTagConfigHandler = commandTagConfigHandler;
    this.equipmentFacade = equipmentFacade;    
    this.equipmentCache = equipmentCache;
    this.equipmentConfigTransacted = equipmentConfigTransacted;
  }

  @Override
  public ProcessChange removeEquipment(final Long equipmentid, final ConfigurationElementReport equipmentReport) {
    LOGGER.debug("Removing Equipment " + equipmentid);
    try {      
      Equipment equipment = equipmentCache.get(equipmentid);
      //WARNING: outside equipment lock, as all these use methods that access a Process (to create ProcessChange object)!
      removeEquipmentTags(equipment, equipmentReport);
      removeEquipmentCommands(equipment, equipmentReport);
      removeSubEquipments(new ArrayList<Long>(equipment.getSubEquipmentIds()), equipmentReport);      
      equipmentCache.acquireWriteLockOnKey(equipmentid);
      try {
        equipmentConfigTransacted.doRemoveEquipment(equipment, equipmentReport);        
        equipmentCache.releaseWriteLockOnKey(equipmentid);
        removeEquipmentControlTags(equipment, equipmentReport); //must be removed last as equipment references them; when this returns are removed from cache and DB permanently
        //remove alive & commfault after control tags, or could be pulled back in from DB to cache!        
        equipmentFacade.removeAliveTimer(equipmentid);
        equipmentFacade.removeCommFault(equipmentid);
        processConfigHandler.removeEquipmentFromProcess(equipmentid, equipment.getProcessId());
        equipmentCache.remove(equipmentid);
        EquipmentUnitRemove equipmentUnitRemove = new EquipmentUnitRemove(0L, equipmentid); //id is reset
        return new ProcessChange(equipment.getProcessId(), equipmentUnitRemove);        
      } finally {
        if (equipmentCache.isWriteLockedByCurrentThread(equipmentid))
          equipmentCache.releaseWriteLockOnKey(equipmentid);
      }      
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.debug("Equipment not found in cache - unable to remove it.");
      equipmentReport.setWarning("Equipment not found in cache so cannot be removed.");
      return new ProcessChange();
    }
  }

  @Override
  public ProcessChange createEquipment(ConfigurationElement element) throws IllegalAccessException {
    ProcessChange change = equipmentConfigTransacted.doCreateEquipment(element);
    equipmentCache.lockAndNotifyListeners(element.getEntityId());
    return change;
  }

  @Override
  public List<ProcessChange> updateEquipment(Long equipmentId, Properties elementProperties) throws IllegalAccessException {
    if (elementProperties.containsKey("processId")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, 
          "Attempting to change the parent process id of an equipment - this is not currently supported!");
    }    
    return commonUpdate(equipmentId, elementProperties);
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
  private void removeSubEquipments(Collection<Long> subEquipmentIds, ConfigurationElementReport equipmentReport) {
    for (Long subEquipmentId : new ArrayList<Long>(subEquipmentIds)) {
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
   * Removes all command tags associated with this equipment.
   * @param equipment reference
   * @param equipmentReport report to add subreports to
   */
  private void removeEquipmentCommands(Equipment equipment, ConfigurationElementReport equipmentReport) {   
    for (Long commandTagId : new ArrayList<Long>(equipment.getCommandTagIds())) { //copy as modified when removing command tag
      ConfigurationElementReport commandReport = new ConfigurationElementReport(Action.REMOVE, Entity.COMMANDTAG, commandTagId);
      equipmentReport.addSubReport(commandReport);
      commandTagConfigHandler.removeCommandTag(commandTagId, commandReport);         
    }
  }
 

}
