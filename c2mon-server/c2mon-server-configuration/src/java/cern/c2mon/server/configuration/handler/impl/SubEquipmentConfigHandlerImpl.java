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

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.SubEquipmentConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.cache.AliveTimerCache;
import cern.tim.server.cache.CommFaultTagCache;
import cern.tim.server.cache.EquipmentCache;
import cern.tim.server.cache.ProcessCache;
import cern.tim.server.cache.SubEquipmentCache;
import cern.tim.server.cache.SubEquipmentFacade;
import cern.tim.server.cache.loading.SubEquipmentDAO;
import cern.tim.server.common.subequipment.SubEquipment;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.common.ConfigurationException;

/**
 * See interface documentation.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class SubEquipmentConfigHandlerImpl extends AbstractEquipmentConfigHandler<SubEquipment> implements SubEquipmentConfigHandler {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(SubEquipmentConfigHandlerImpl.class);
  
  /**
   * Facade.
   */
  private SubEquipmentFacade subEquipmentFacade;
  
  /**
   * Cache.
   */
  private SubEquipmentCache subEquipmentCache;
  
  /**
   * DAO.
   */
  private SubEquipmentDAO subEquipmentDAO;
  
  private EquipmentCache equipmentCache;
  
  /**
   * Autowired constructor.
   */
  @Autowired
  public SubEquipmentConfigHandlerImpl(ControlTagConfigHandler controlTagConfigHandler, SubEquipmentFacade subEquipmentFacade,
                                   SubEquipmentCache subEquipmentCache, SubEquipmentDAO subEquipmentDAO,
                                   AliveTimerCache aliveTimerCache , CommFaultTagCache commFaultTagCache,
                                   ProcessCache processCache, EquipmentCache equipmentCache) {
    super(controlTagConfigHandler, subEquipmentFacade, subEquipmentCache,
          subEquipmentDAO, aliveTimerCache, commFaultTagCache);
    this.subEquipmentFacade = subEquipmentFacade;
    this.subEquipmentCache = subEquipmentCache;
    this.subEquipmentDAO = subEquipmentDAO;
    this.equipmentCache = equipmentCache;
  }

  /**
   * Creates the SubEquipment cache object and puts it into the cache
   * and DB. The alive and commfault tag caches are updated also.
   * The Equipment cache object is updated to include the new
   * SubEquipment.
   * 
   * @param element details of configuration
   * @throws IllegalAccessException should not be thrown here (in common interface for Tags)
   */
  @Override
  @Transactional("cacheTransactionManager")
  public ProcessChange createSubEquipment(final ConfigurationElement element) throws IllegalAccessException {
    SubEquipment subEquipment = super.createAbstractEquipment(element);
    subEquipmentFacade.addSubEquipmentToEquipment(subEquipment.getId(), subEquipment.getParentId());
    return new ProcessChange(subEquipmentFacade.getEquipmentForSubEquipment(subEquipment.getId()).getProcessId());
  }
  
  public List<ProcessChange> updateSubEquipment(Long subEquipmentId, Properties properties) throws IllegalAccessException {
    if (properties.containsKey("parent_equip_id")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, 
          "Attempting to change the parent equipment id of a subequipment - this is not currently supported!");
    }
    SubEquipment subEquipment = subEquipmentCache.get(subEquipmentId);
    subEquipment.getWriteLock().lock();
    try {        
      return super.updateAbstractEquipment(subEquipment, properties);
    } catch (UnexpectedRollbackException ex) {    
      equipmentCache.remove(subEquipment.getParentId());
      throw ex;
    } finally {
      subEquipment.getWriteLock().unlock();
    }
  }

  /**
   * First removes the SubEquipment from the DB and cache. If successful,
   * removes the associated control tags. 
   * 
   * <p>If an exception is thrown the SubEquipment will be restored in DB (transaction rollback).
   *  
   * @param subEquipmentId id
   * @param subEquipmentReport to which subreports may be added
   */
  @Override
  @Transactional("cacheTransactionManager")
  public ProcessChange removeSubEquipment(final Long subEquipmentId, final ConfigurationElementReport subEquipmentReport) {
    LOGGER.debug("Removing SubEquipment " + subEquipmentId);
    if (subEquipmentCache.hasKey(subEquipmentId)) {
      SubEquipment subEquipment = subEquipmentCache.get(subEquipmentId);    
      try {
        //remove alive and commfault before locking (lock hierarchy)
        subEquipmentFacade.removeAliveTimer(subEquipmentId);
        subEquipmentFacade.removeCommFault(subEquipmentId);
        subEquipment.getWriteLock().lock();      
        subEquipmentDAO.deleteItem(subEquipmentId);
        subEquipmentCache.remove(subEquipmentId);
        removeEquipmentControlTags(subEquipment, subEquipmentReport); //must be after removal of subequipment from DB        
        return new ProcessChange(equipmentCache.get(subEquipment.getParentId()).getProcessId());
      } finally {
        if (subEquipment.getWriteLock().isHeldByCurrentThread()) {
          subEquipment.getWriteLock().unlock();
        }        
      } 
    } else {
      LOGGER.debug("SubEquipment not found in cache - unable to remove it.");
      subEquipmentReport.setWarning("SubEquipment not found in cache so cannot be removed.");
      return new ProcessChange(); 
    }      
  }

}
