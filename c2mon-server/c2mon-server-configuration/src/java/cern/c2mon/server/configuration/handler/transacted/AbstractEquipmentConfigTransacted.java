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
package cern.c2mon.server.configuration.handler.transacted;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.transaction.UnexpectedRollbackException;

import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.equipment.CommonEquipmentFacade;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.EquipmentConfigurationUpdate;

/**
 * Common functionalities for configuring Equipment and SubEquipment.
 * 
 * @param <T>
 *          the type held in the cache
 * @author Mark Brightwell
 * 
 */
public abstract class AbstractEquipmentConfigTransacted<T extends AbstractEquipment> implements CommonEquipmentConfigTransacted<T> {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AbstractEquipmentConfigTransacted.class);

  /**
   * The ConfigHandler for ControlTags.
   */
  protected ControlTagConfigHandler controlTagConfigHandler;

  /**
   * The facade to the cache objects (common methods implemented for both
   * Equipment and SubEquipment).
   */
  private CommonEquipmentFacade<T> commonEquipmentFacade;

  /**
   * The cache.
   */
  private C2monCache<Long, T> abstractEquipmentCache;

  /**
   * The DB acces object.
   */
  private ConfigurableDAO<T> configurableDAO;

  /**
   * Facade to alive timer.
   */
  private AliveTimerCache aliveTimerCache;

  /**
   * Facade to CommFaultTag.
   */
  private CommFaultTagCache commFaultTagCache;

  /**
   * Constructor.
   * 
   * @param controlTagConfigHandler
   *          the ConfigHandler for ControlTags
   * @param commonEquipmentFacade
   *          the Facade bean for the (Sub)Equipment
   * @param abstractEquipmentCache
   *          the Cache bean for the (Sub)Equipment
   * @param configurableDAO
   *          the DAO for the (Sub)Equipment
   * @param commFaultTagCache
   *          ref to cache
   * @param aliveTimerCache
   *          ref to cache
   */
  protected AbstractEquipmentConfigTransacted(final ControlTagConfigHandler controlTagConfigHandler, final CommonEquipmentFacade<T> commonEquipmentFacade,
      final C2monCache<Long, T> abstractEquipmentCache, final ConfigurableDAO<T> configurableDAO, final AliveTimerCache aliveTimerCache,
      final CommFaultTagCache commFaultTagCache) {
    super();
    this.controlTagConfigHandler = controlTagConfigHandler;
    this.commonEquipmentFacade = commonEquipmentFacade;
    this.abstractEquipmentCache = abstractEquipmentCache;
    this.configurableDAO = configurableDAO;
    this.aliveTimerCache = aliveTimerCache;
    this.commFaultTagCache = commFaultTagCache;
  }

  /**
   * Creates the abstract equipment, puts it in the DB and then loads it into
   * the cache (in that order). The AliveTimer and CommFaultTag are then
   * generated in their respective caches.
   * 
   * @param element
   *          contains the creation detais
   * @return the generated AbstractEquipment object
   * @throws IllegalAccessException
   *           should not be thrown here (inherited at interface from Tag
   *           creation).
   */
  protected T createAbstractEquipment(final ConfigurationElement element) throws IllegalAccessException {
    abstractEquipmentCache.acquireWriteLockOnKey(element.getEntityId());
    try {
      LOGGER.debug("Creating (Sub)Equipment " + element.getEntityId());
      T abstractEquipment = commonEquipmentFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
      try {
        configurableDAO.insert(abstractEquipment);
        abstractEquipmentCache.putQuiet(abstractEquipment);
        
        // clear alive and commfault caches and refresh
        // (synch ok as locked equipment so no changes to these ids)
        if (abstractEquipment.getAliveTagId() != null) {
          commonEquipmentFacade.loadAndStartAliveTag(abstractEquipment.getId());
        }
        if (abstractEquipment.getCommFaultTagId() != null) {
          commFaultTagCache.remove(abstractEquipment.getCommFaultTagId());
          commFaultTagCache.loadFromDb(abstractEquipment.getCommFaultTagId());
        }
        
      } catch (Exception e) {
        if (abstractEquipment.getAliveTagId() != null) {
          aliveTimerCache.remove(abstractEquipment.getId());
        }
        if (abstractEquipment.getCommFaultTagId() != null) {
          commFaultTagCache.remove(abstractEquipment.getCommFaultTagId());
        }
        throw new UnexpectedRollbackException("Exception caught while creating equipment: rolling back changes", e);
      }
      // TODO necessary to use DB loading or not?? to check...
      // removed as now rely on automatic cache loading from DB: problem: also
      // used in checking if tag is alive or commfault, so added again
      // abstractEquipmentCache.putQuiet(abstractEquipment);
      // aliveTimerFacade.generateFromEquipment(abstractEquipment);
      // commFaultTagFacade.generateFromEquipment(abstractEquipment);
      return abstractEquipment;
    } finally {
      abstractEquipmentCache.releaseWriteLockOnKey(element.getEntityId());
    }
  }

  /**
   * Can be called directly for both Equipment and SubEquipment configuration
   * updates.
   * 
   * <p>
   * Updates made to Equipments are done programmatically both in cache and DB,
   * to avoid reloading the Equipment from the DB at runtime, which can be
   * time-consuming if many tags are declared.
   * 
   * <p>
   * Should be called within the AbstractEquipment write lock.
   * 
   * <p>
   * Should not throw the {@link IllegalAccessException} (only Tags can).
   * 
   * @param equipmentId
   *          of the (Sub)Equipment
   * @param properties
   *          containing the changed fields
   * @return ProcessChange used only for Equipment reconfiguration (not
   *         SubEquipment)
   * @throws IllegalAccessException
   *           not thrown for Equipment
   */  
  protected List<ProcessChange> updateAbstractEquipment(final T abstractEquipment, final Properties properties) throws IllegalAccessException {
    EquipmentConfigurationUpdate equipmentUpdate;
    equipmentUpdate = (EquipmentConfigurationUpdate) commonEquipmentFacade.updateConfig(abstractEquipment, properties);
    configurableDAO.updateConfig(abstractEquipment);
    
    // create change event for DAQ layer
    Long processId = commonEquipmentFacade.getProcessIdForAbstractEquipment(abstractEquipment.getId());
    ArrayList<ProcessChange> processChanges = new ArrayList<ProcessChange>();
    processChanges.add(new ProcessChange(commonEquipmentFacade.getProcessIdForAbstractEquipment(abstractEquipment.getId()), equipmentUpdate));
    // if alive tags associated to equipment are changed and have an address,
    // inform DAQ also (use same changeId so these become sub-reports of the
    // correct report)
    if (equipmentUpdate.getAliveTagId() != null) {
      ProcessChange processChange = controlTagConfigHandler.getCreateEvent(equipmentUpdate.getChangeId(), abstractEquipment.getAliveTagId(), abstractEquipment
          .getId(), processId);
      // null if this alive does not have an Address -> is not in list of
      // DataTags on DAQ
      if (processChange != null) {
        processChanges.add(processChange);
      }
    }
    return processChanges;
  }

}
