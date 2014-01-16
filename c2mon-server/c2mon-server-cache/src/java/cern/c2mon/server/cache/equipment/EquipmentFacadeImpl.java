/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.cache.equipment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.AliveTimerFacade;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.CommFaultTagFacade;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.EquipmentConfigurationUpdate;

/**
 * Implementation of EquipmentFacade bean.
 * @author Mark Brightwell
 *
 */
@Service
public class EquipmentFacadeImpl extends AbstractEquipmentFacade<Equipment> implements EquipmentFacade {
  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(EquipmentFacadeImpl.class); 
  
  /**
   * Ref to process cache.
   */
  private ProcessCache processCache;

  /**
   * Constructor
   * @param equipmentCache equipment cache
   * @param processCache process cache
   * @param aliveTimerFacade alive timer facade
   * @param commFaultTagFacade commFaultTag facade
   * @param commFaultTagCache commFaultTag cache
   * @param aliveTimerCache alive timer cache
   */
  @Autowired
  public EquipmentFacadeImpl(final EquipmentCache equipmentCache,                                    
                             final ProcessCache processCache, 
                             final AliveTimerFacade aliveTimerFacade, final CommFaultTagFacade commFaultTagFacade, 
                             final CommFaultTagCache commFaultTagCache, final AliveTimerCache aliveTimerCache) {
    super(equipmentCache, aliveTimerFacade, aliveTimerCache, commFaultTagCache, commFaultTagFacade);    
    this.processCache = processCache;
    this.aliveTimerFacade = aliveTimerFacade;      
  }
  
  /**
   * Creates the equipment object from the properties and validates them.
   * The returned object has not been inserted in the cache.
   */
  public Equipment createCacheObject(final Long equipmentId, final Properties properties) {
    EquipmentCacheObject equipment = new EquipmentCacheObject(equipmentId);    
    configureCacheObject(equipment, properties);
    validateConfig(equipment);
    return equipment;
  }
  
  /**
   * Overridden as for Equipment rule out updating the process this equipment
   * is associated to.
   * @throws IllegalAccessException 
   */
  @Override
  public Change updateConfig(Equipment equipment, Properties properties) throws IllegalAccessException { 
    if ((properties.getProperty("processId")) != null) {      
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Reconfiguration of " 
          + "Equipment does not currently allow it to be reassigned to a different Process!");      
    }
    return super.updateConfig(equipment, properties);      
  }
  
  /**
   * Throws exceptions if the equipment does not have valid
   * field formats.
   * 
   * @param equipment
   * @throws ConfigurationException if a field is not formatted correctly
   */
  protected void validateConfig(Equipment equipment) {
    EquipmentCacheObject equipmentCacheObject = (EquipmentCacheObject) equipment;
    super.validateConfig(equipmentCacheObject);
    //only validated for Equipment, although also set in SubEquipment (but not used there - only DB related and inherited from TIM1! -> TODO remove handler class name form subequipment cache object and adapt loading SQL)
    if (equipmentCacheObject.getHandlerClassName() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"handlerClassName\" cannot be null");
    }

    if (equipmentCacheObject.getProcessId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"processId\" cannot be null. An equipment MUST be attached to a process.");
    }
  }

  /**
   * Sets the Equipment fields from the passed properties.
   * @param equipment
   * @param properties
   * @return a change event to be sent to the DAQ layer
   */
  protected EquipmentConfigurationUpdate configureCacheObject(Equipment equipment, Properties properties) {
    EquipmentCacheObject equipmentCacheObject = (EquipmentCacheObject) equipment;
    EquipmentConfigurationUpdate configurationUpdate = setCommonProperties(equipmentCacheObject, properties);
    String tmpStr = properties.getProperty("address");
    if (tmpStr != null) {
      equipmentCacheObject.setAddress(tmpStr);      
      configurationUpdate.setEquipmentAddress(tmpStr);
    }    

    //never set when called from config update method
    if ((tmpStr = properties.getProperty("processId")) != null) {
      try {
        equipmentCacheObject.setProcessId(Long.valueOf(tmpStr));        
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"processId\" to Long: " + tmpStr);
      }
    }
    return configurationUpdate;
  }

  public Long getProcessIdForAbstractEquipment(final Long equipmentId) {
    Equipment equipment = cache.get(equipmentId);    
    Long processId = equipment.getProcessId();
    if (processId == null) {
      throw new NullPointerException("Equipment " + equipmentId + "has no associated Process id - this should never happen!");
    } else {
      return processId;
    }        
  }
  
  @Override
  public Collection<Long> getDataTagIds(Long equipmentId) {
    Equipment equipment = cache.getCopy(equipmentId);
    return equipment.getDataTagIds();       
  }
  
  @Override
  public void addTagToEquipment(Long equipmentId, Long dataTagId) {
    cache.acquireWriteLockOnKey(equipmentId);    
    try {
      Equipment equipment = cache.get(equipmentId);
      equipment.getDataTagIds().add(dataTagId);
    } finally {
      cache.releaseWriteLockOnKey(equipmentId);
    }
  }

  @Override
  public void removeTagFromEquipment(Long equipmentId, Long dataTagId) {
    cache.acquireWriteLockOnKey(equipmentId);
    try {      
      Equipment equipment = cache.get(equipmentId);
      equipment.getDataTagIds().remove(dataTagId);
    } finally {
      cache.releaseWriteLockOnKey(equipmentId);     
    }
  }
  
  @Override
  public void addCommandToEquipment(Long equipmentId, Long commandId) {
    cache.acquireWriteLockOnKey(equipmentId);
    try {
      Equipment equipment = cache.get(equipmentId);      
      equipment.getCommandTagIds().add(commandId);
    } finally {
      cache.releaseWriteLockOnKey(equipmentId);      
    }
  }
  
  @Override
  public void removeCommandFromEquipment(Long equipmentId, Long commandId) {
    cache.acquireWriteLockOnKey(equipmentId);    
    try {
      Equipment equipment = cache.get(equipmentId);      
      equipment.getCommandTagIds().remove(commandId);
    } finally {
      cache.releaseWriteLockOnKey(equipmentId);       
    }
  }
  
  @Override
  public Collection<Long> getEquipmentAlives() {
    List<Long> aliveIds = new ArrayList<Long>(100);
    List<Long> keys = cache.getKeys();    
    for (Long id : keys) {      
      Equipment equipment = cache.getCopy(id);      
      Long aliveId = equipment.getAliveTagId();
      if (aliveId != null) {
        aliveIds.add(aliveId);
      }                 
    }
    return aliveIds;
  }

  @Override
  public void addEquipmentToProcess(Long equipmentId, Long processId) {
    LOGGER.trace("Adding Equipment to Process");
    processCache.acquireWriteLockOnKey(processId);
    try {
      Process process = processCache.get(processId);
      if (process.getEquipmentIds().contains(equipmentId)) {
        LOGGER.warn("Trying to add existing Equipment to a Process!");
      } else {
        process.getEquipmentIds().add(equipmentId);
      }
    } finally {
      processCache.releaseWriteLockOnKey(processId);
    }
  }

  @Override
  protected SupervisionEntity getSupervisionEntity() {
   return SupervisionEntity.EQUIPMENT;
  }
  
}
