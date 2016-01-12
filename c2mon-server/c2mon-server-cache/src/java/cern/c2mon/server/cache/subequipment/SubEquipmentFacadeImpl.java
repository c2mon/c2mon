/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.cache.subequipment;

import java.util.Collection;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.AliveTimerFacade;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.CommFaultTagFacade;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.SubEquipmentFacade;
import cern.c2mon.server.cache.equipment.AbstractEquipmentFacade;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(SubEquipmentFacadeImpl.class);

  /**
   * Equipment cache bean.
   */
  private EquipmentCache equipmentCache;

  /**
   * Autowired constructor.
   */
  @Autowired
  public SubEquipmentFacadeImpl(final SubEquipmentCache subEquipmentCache,
                                final EquipmentCache equipmentCache,
                                final AliveTimerFacade aliveTimerFacade,
                                final AliveTimerCache aliveTimerCache,
                                final CommFaultTagCache commFaultTagCache,
                                final CommFaultTagFacade commFaultTagFacade) {
    super(subEquipmentCache, aliveTimerFacade, aliveTimerCache, commFaultTagCache, commFaultTagFacade);
    this.equipmentCache = equipmentCache;
  }

  @Override
  public SubEquipment createCacheObject(Long id, Properties properties){
    SubEquipmentCacheObject subEquipment = new SubEquipmentCacheObject(id);
    configureCacheObject(subEquipment, properties);
    validateConfig(subEquipment);
    return subEquipment;
  }

  /**
   * Sets the fields particular for SubEquipment from the properties object.
   * @param subEquipment sets the fields in this object
   * @param properties looks for relevant properties in this object
   */
  @Override
  protected Change configureCacheObject(SubEquipment subEquipment, Properties properties) {
    SubEquipmentCacheObject subEquipmentCacheObject = (SubEquipmentCacheObject) subEquipment;
    EquipmentConfigurationUpdate update = setCommonProperties(subEquipment, properties);
    String tmpStr = properties.getProperty("equipmentId");

    // TODO: Remove obsolete parent_equip_id property
    if (tmpStr == null) {
      tmpStr = properties.getProperty("parent_equip_id");
    }

    if (tmpStr != null) {
      try {
        subEquipmentCacheObject.setParentId(Long.valueOf(tmpStr));
      } catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"parentId\" to Long: " + tmpStr);
      }
    }

    return update;
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
    // TODO: Remove obsolete parent_equip_id property
    if ((properties.getProperty("parent_equip_id")) != null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Reconfiguration of "
          + "SubEquipment does not currently allow it to be reassigned to a different Equipment!");
    }

    if ((properties.getProperty("equipmentId")) != null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Reconfiguration of "
          + "SubEquipment does not currently allow it to be reassigned to a different Equipment!");
    }

    super.updateConfig(subEquipment, properties);
    return new EquipmentConfigurationUpdate();
  }

  /**
   * Throws an exception if the validation fails.
   *
   * @param subEquipment the SubEquipment to validate
   * @throws ConfigurationException if the validation fails
   */
  @Override
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
        equipmentCache.putQuiet(equipment);
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

  @Override
  public void addTagToSubEquipment(Long subEquipmentId, Long dataTagId) {
    cache.acquireWriteLockOnKey(subEquipmentId);
    try {
      SubEquipment subEquipment = cache.get(subEquipmentId);
      subEquipment.getDataTagIds().add(dataTagId);
      cache.putQuiet(subEquipment);
    } finally {
      cache.releaseWriteLockOnKey(subEquipmentId);
    }
  }

  @Override
  public void removeTagFromSubEquipment(Long subEquipmentId, Long dataTagId) {
    cache.acquireWriteLockOnKey(subEquipmentId);
    try {
      SubEquipment subEquipment = cache.get(subEquipmentId);
      subEquipment.getDataTagIds().remove(dataTagId);
      cache.putQuiet(subEquipment);
    } finally {
      cache.releaseWriteLockOnKey(subEquipmentId);
    }
  }

  @Override
  public Collection<Long> getDataTagIds(Long subEquipmentId) {
    SubEquipment subEquipment = cache.getCopy(subEquipmentId);
    return subEquipment.getDataTagIds();
  }

  @Override
  public void removeSubEquipmentFromEquipment(Long equipmentId, Long subEquipmentId) {
    cache.acquireWriteLockOnKey(equipmentId);
    try {
      Equipment equipment = equipmentCache.get(equipmentId);
      equipment.getSubEquipmentIds().remove(subEquipmentId);
      equipmentCache.putQuiet(equipment);
    } finally {
      cache.releaseWriteLockOnKey(equipmentId);
    }
  }
}
