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
package cern.c2mon.server.cache.equipment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.AliveTimerFacade;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.CommFaultTagFacade;
import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.C2monCacheWithListeners;
import cern.c2mon.server.cache.common.AbstractSupervisedFacade;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.common.equipment.AbstractEquipmentCacheObject;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.EquipmentConfigurationUpdate;

/**
 * Abstract class implementing common AbstractEquipment logic, 
 * to be used in Equipement and Subequipment facade objects.
 * 
 * @author Mark Brightwell
 * 
 * @param <T> type of cache object
 *
 */
public abstract class AbstractEquipmentFacade<T extends AbstractEquipment> extends AbstractSupervisedFacade<T> implements CommonEquipmentFacade<T> {

  protected C2monCacheWithListeners<Long, T> cache;
  
  protected AliveTimerFacade aliveTimerFacade;
  
  protected AliveTimerCache aliveTimerCache;
  
  private CommFaultTagCache commFaultTagCache;
  
  private CommFaultTagFacade commFaultTagFacade;
  
  public AbstractEquipmentFacade(C2monCacheWithListeners<Long, T> cache, AliveTimerFacade aliveTimerFacade, 
                            AliveTimerCache aliveTimerCache, CommFaultTagCache commFaultTagCache,
                            CommFaultTagFacade commFaultTagFacade) {
    super(cache, aliveTimerCache, aliveTimerFacade);
    this.cache = cache;
    this.aliveTimerFacade = aliveTimerFacade;
    this.aliveTimerCache = aliveTimerCache;
    this.commFaultTagCache = commFaultTagCache;
    this.commFaultTagFacade = commFaultTagFacade;
  }

  
  /**
   * Overridden as may need to reset commfault and 
   * alivetimer caches also.
   * @throws IllegalAccessException 
   */
  @Override
  public Change updateConfig(T abstractEquipment, Properties properties) throws IllegalAccessException { 
    Change change = super.updateConfig(abstractEquipment, properties);
    return change;
  }
  
  /**
   * Sets the common properties for Equipment and Subequipment cache objects
   * when creating or updating them.
   * 
   * @param abstractEquipmentCacheObject
   * @param properties
   */
  protected EquipmentConfigurationUpdate setCommonProperties(T abstractEquipment, Properties properties) {
    AbstractEquipmentCacheObject  abstractEquipmentCacheObject = (AbstractEquipmentCacheObject) abstractEquipment;
    EquipmentConfigurationUpdate configurationUpdate = new EquipmentConfigurationUpdate();
    configurationUpdate.setEquipmentId(abstractEquipment.getId());
    String tmpStr = null;

    // Set the process name and all parameters DERIVED from the process name
    tmpStr = properties.getProperty("name");
    if (tmpStr != null) {
      abstractEquipmentCacheObject.setName(tmpStr);
      configurationUpdate.setName(tmpStr);
    }
      
    if (properties.getProperty("description") != null)
      abstractEquipmentCacheObject.setDescription(properties.getProperty("description"));

    if ((tmpStr = properties.getProperty("aliveTagId")) != null) {      
      try {
        Long aliveId = Long.valueOf(tmpStr);      
        abstractEquipmentCacheObject.setAliveTagId(aliveId);
        configurationUpdate.setAliveTagId(aliveId);
      } catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"aliveTagId\" to Long: " + tmpStr);
      }
    }

    if ((tmpStr = properties.getProperty("aliveInterval")) != null) {
      try {        
        abstractEquipmentCacheObject.setAliveInterval(Integer.parseInt(tmpStr));
        configurationUpdate.setAliveInterval(Long.parseLong(tmpStr));
      } catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"aliveInterval\" to Integer: " + tmpStr);
      }
    }

    if ((tmpStr = properties.getProperty("stateTagId")) != null) {
      try {
        abstractEquipmentCacheObject.setStateTagId(Long.valueOf(tmpStr));
      } catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"stateTagId\" to Long: " + tmpStr);
      }
    }

    if (properties.getProperty("handlerClass") != null) {
      abstractEquipmentCacheObject.setHandlerClassName(properties.getProperty("handlerClass"));
    }   

    if ((tmpStr = properties.getProperty("commFaultTagId")) != null) {
      try {
        Long commfaultTagId = Long.valueOf(tmpStr);
        abstractEquipmentCacheObject.setCommFaultTagId(commfaultTagId);
        configurationUpdate.setCommfaultTagId(commfaultTagId);        
      } catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"commFaultTagId\" to Long: " + tmpStr);
      }
    }
    return configurationUpdate;
  }
  
  protected void validateConfig(T abstractEquipment) {
    AbstractEquipmentCacheObject  abstractEquipmentCacheObject = (AbstractEquipmentCacheObject) abstractEquipment;
    if (abstractEquipmentCacheObject.getId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
    }
    if (abstractEquipmentCacheObject.getName() == null) { 
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be null");
    }
    if (abstractEquipmentCacheObject.getName().length() == 0 || abstractEquipmentCacheObject.getName().length() > 60) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" must be 1 to 60 characters long");
    }
    if (abstractEquipmentCacheObject.getDescription() == null) { 
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" cannot be null");
    }
    if (abstractEquipmentCacheObject.getDescription().length() == 0 || abstractEquipmentCacheObject.getDescription().length() > 100) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" must be 1 to 100 characters long");
    }    
    if (abstractEquipmentCacheObject.getStateTagId() == null ) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"stateTagId\" cannot be null. Each equipment MUST have a registered state tag.");
    }
    if (abstractEquipmentCacheObject.getCommFaultTagId() == null ) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"commFaultTagId\" cannot be null. Each equipment MUST have a registered communication fault tag.");
    }
    if (abstractEquipmentCacheObject.getAliveInterval() < 10000) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"aliveInterval\" must be >= 10000 milliseconds. It makes no sense to send the alive tag too often.");
    }    
  }
  
  @Override
  public void removeCommFault(final Long abstractEquipmentId) {
    T equipment = cache.get(abstractEquipmentId);
    Long commFaultId = equipment.getCommFaultTagId(); 
    if (commFaultId != null) {
      commFaultTagCache.remove(commFaultId);
    }
  }
  
  @Override
  public Map<Long, Long> getAbstractEquipmentControlTags() {
    HashMap<Long, Long> returnMap = new HashMap<Long, Long>();  
    List<Long> equipmentKeys = cache.getKeys(); 
    for (Long equipmentId : equipmentKeys) {      
      AbstractEquipment equipment = cache.getCopy(equipmentId);            
      Long aliveId = equipment.getAliveTagId();
      if (aliveId != null) {
        returnMap.put(aliveId, equipmentId);
      }
      Long stateId = equipment.getStateTagId();
      if (stateId != null) {
        returnMap.put(stateId, equipmentId);
      }
      Long commFaultId = equipment.getCommFaultTagId();
      if (commFaultId != null) {
        returnMap.put(commFaultId, equipmentId);
      }            
    }
    return returnMap;
  }
  
}
