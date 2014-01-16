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
package cern.c2mon.server.cache.subequipment;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.AliveTimerFacade;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.CommFaultTagFacade;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.SubEquipmentFacade;
import cern.c2mon.server.cache.equipment.AbstractEquipmentFacade;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.EquipmentConfigurationUpdate;

/**
 * Implementation of the SubEquipmentFacade.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class SubEquipmentFacadeImpl extends AbstractEquipmentFacade<SubEquipment> implements SubEquipmentFacade {
  
  private static final Logger LOGGER = Logger.getLogger(SubEquipmentFacadeImpl.class); 
  
  /**
   * DAO, still necessary for code ported from TIM1.
   */
  private SubEquipmentDAO subEquipmentDAO;
  
  /**
   * Equipment cache bean.
   */
  private EquipmentCache equipmentCache;
  
  /**
   * Process cache bean.
   */
  private ProcessCache processCache;
  
  /**
   * Autowired constructor.
   */
  @Autowired
  public SubEquipmentFacadeImpl(final SubEquipmentDAO subEquipmentDAO, final SubEquipmentCache subEquipmentCache, 
                                final EquipmentCache equipmentCache, final AliveTimerFacade aliveTimerFacade,
                                final AliveTimerCache aliveTimerCache, final CommFaultTagCache commFaultTagCache, 
                                final CommFaultTagFacade commFaultTagFacade, final ProcessCache processCache) {
    super(subEquipmentCache, aliveTimerFacade, aliveTimerCache, commFaultTagCache, commFaultTagFacade);
    this.subEquipmentDAO = subEquipmentDAO;
    this.equipmentCache = equipmentCache;
    this.processCache = processCache;
  }

  @Override
  public SubEquipment createCacheObject(Long id, Properties properties){
    SubEquipmentCacheObject subEquipment = new SubEquipmentCacheObject(id);
    setCommonProperties(subEquipment, properties);
    configureCacheObject(subEquipment, properties);
    validateConfig(subEquipment);
    return subEquipment;
  }
  
  /**
   * Sets the fields particular for SubEquipment from the properties object.
   * @param subEquipment sets the fields in this object
   * @param properties looks for relevant properties in this object
   */
  protected Change configureCacheObject(SubEquipment subEquipment, Properties properties) {
    SubEquipmentCacheObject subEquipmentCacheObject = (SubEquipmentCacheObject) subEquipment;
    String tmpStr = null;

    if ((tmpStr = properties.getProperty("parent_equip_id")) != null) {
      try {
        subEquipmentCacheObject.setParentId(Long.valueOf(tmpStr));
      } catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"parentId\" to Long: " + tmpStr);
      }
    }
    return null;
  }
  
  @Override
  public Long getEquipmentIdForSubEquipment(final Long subEquipmentId) {
    SubEquipment subEquipment = cache.get(subEquipmentId);
    Long equipmentId = subEquipment.getParentId();
    if (equipmentId == null) {
      throw new NullPointerException("SubEquipment " + subEquipmentId + "has no associated Equipment id (parent id) - this should never happen!");
    } else {
      return equipmentId;
    }
  }
  
  /**
   * Overridden as for SubEquipment rule out changing the parent equipment
   * associated it is associated to.
   * @throws IllegalAccessException 
   * @return empty EquipmentConfigurationUpdate because SubEquipments are not used
   *          on the DAQ layer and no event is sent (return type necessary as in 
   *          common interface). 
   */
  @Override
  public EquipmentConfigurationUpdate updateConfig(final SubEquipment subEquipment, final Properties properties) throws IllegalAccessException { 
    if ((properties.getProperty("parent_equip_id")) != null) {      
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Reconfiguration of " 
          + "SubEquipment does not currently allow it to be reassigned to a different Equipment!");      
    }
    super.updateConfig(subEquipment, properties); 
    return new EquipmentConfigurationUpdate();
  }
  
  /**
   * Throws an exception if the validation fails.
   * 
   * @param subEquipmentCacheObject the SubEquipment to validate
   * @throws ConfigurationException if the validation fails
   */
  protected void validateConfig(final SubEquipment subEquipment) {
    SubEquipmentCacheObject subEquipmentCacheObject = (SubEquipmentCacheObject) subEquipment;
    super.validateConfig(subEquipmentCacheObject);
    if (subEquipmentCacheObject.getParentId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"parentId\" cannot be null. Each Subequipment MUST be attached to an Equipment.");
    }
  }

  @Override
  public void addSubEquipmentToEquipment(Long id, Long parentId) {
    equipmentCache.acquireWriteLockOnKey(parentId);   
    try {
      Equipment equipment = equipmentCache.get(parentId);     
      if (equipment.getSubEquipmentIds().contains(id)) {
        LOGGER.warn("Trying to add existing SubEquipment to an Equipment!");
      } else {
        equipment.getSubEquipmentIds().add(id);
      }
    } finally {
      equipmentCache.releaseWriteLockOnKey(parentId);
    }
  }

  @Override
  public Long getProcessIdForAbstractEquipment(Long abstractEquipmentId) {
    Equipment equipment = equipmentCache.get(getEquipmentIdForSubEquipment(abstractEquipmentId));        
    Long processId = equipment.getProcessId();
    if (processId == null) {
      throw new NullPointerException("Equipment " + equipment.getId() + "has no associated Process id - this should never happen!");
    } else {
      return processId;
    }
  }

  @Override
  protected SupervisionEntity getSupervisionEntity() {
    return SupervisionEntity.SUBEQUIPMENT;
  }
  
}
