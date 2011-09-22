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
import org.springframework.transaction.UnexpectedRollbackException;

import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.cache.AliveTimerCache;
import cern.tim.server.cache.CommFaultTagCache;
import cern.tim.server.cache.ProcessCache;
import cern.tim.server.cache.TimCache;
import cern.tim.server.cache.equipment.CommonEquipmentFacade;
import cern.tim.server.cache.loading.ConfigurableDAO;
import cern.tim.server.common.equipment.AbstractEquipment;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.client.configuration.ConfigConstants.Action;
import cern.tim.shared.client.configuration.ConfigConstants.Entity;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.daq.config.EquipmentConfigurationUpdate;

/**
 * Common functionalities for configuring Equipment and SubEquipment.
 * 
 * @param <T> the type held in the cache
 * @author Mark Brightwell
 *
 */
public abstract class AbstractEquipmentConfigHandler<T extends AbstractEquipment> {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AbstractEquipmentConfigHandler.class); 
  
  /**
   * The ConfigHandler for ControlTags.
   */
  protected ControlTagConfigHandler controlTagConfigHandler;
  
  /**
   * The facade to the cache objects (common methods implemented
   * for both Equipment and SubEquipment).
   */ 
  private CommonEquipmentFacade<T> commonEquipmentFacade;
  
  /**
   * The cache.
   */
  private TimCache<T> abstractEquipmentCache;
  
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
   * Reference to process cache, needed for cleaning on configuration errors.
   */
  private ProcessCache processCache;
  
  /**
   * Constructor.
   * @param controlTagConfigHandler the ConfigHandler for ControlTags
   * @param commonEquipmentFacade the Facade bean for the (Sub)Equipment
   * @param abstractEquipmentCache the Cache bean for the (Sub)Equipment
   * @param configurableDAO the DAO for the (Sub)Equipment
   * @param commFaultTagCache ref to cache 
   * @param aliveTimerCache ref to cache
   */
  protected AbstractEquipmentConfigHandler(
      final ControlTagConfigHandler controlTagConfigHandler,     
      final CommonEquipmentFacade<T> commonEquipmentFacade,
      final TimCache<T> abstractEquipmentCache, final ConfigurableDAO<T> configurableDAO,
      final AliveTimerCache aliveTimerCache, final CommFaultTagCache commFaultTagCache) {
    super();
    this.controlTagConfigHandler = controlTagConfigHandler;
    this.commonEquipmentFacade = commonEquipmentFacade;
    this.abstractEquipmentCache = abstractEquipmentCache;
    this.configurableDAO = configurableDAO;
    this.aliveTimerCache = aliveTimerCache;
    this.commFaultTagCache = commFaultTagCache;   
  }
  
  /**
   * Creates the abstract equipment, puts it in the DB and then loads it into the cache (in that order).
   * The AliveTimer and CommFaultTag are then generated in their respective caches.
   * @param element contains the creation detais
   * @return the generated AbstractEquipment object
   * @throws IllegalAccessException should not be thrown here (inherited at interface from Tag creation).
   */
  protected T createAbstractEquipment(final ConfigurationElement element) throws IllegalAccessException {  
    LOGGER.debug("Creating (Sub)Equipment " + element.getEntityId());
    T abstractEquipment = commonEquipmentFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
    configurableDAO.insert(abstractEquipment);
    
    //clear alive and commfault caches
    //(synch ok as locked equipment so no changes to these ids)
    if (abstractEquipment.getAliveTagId() != null) {
      aliveTimerCache.remove(abstractEquipment.getAliveTagId());
    }
    if (abstractEquipment.getCommFaultTagId() != null) {
      commFaultTagCache.remove(abstractEquipment.getCommFaultTagId());
    }
       
    //TODO necessary to use DB loading or not?? to check...
    //removed as now rely on automatic cache loading from DB
//    abstractEquipmentCache.putQuiet(abstractEquipment);    
//    aliveTimerFacade.generateFromEquipment(abstractEquipment); 
//    commFaultTagFacade.generateFromEquipment(abstractEquipment);
    return abstractEquipment;
  }

  /**
   * Can be called directly for both Equipment and SubEquipment configuration updates.
   * 
   * <p>Updates made to Equipments are done programmatically both in cache and DB, to
   * avoid reloading the Equipment from the DB at runtime, which can be time-consuming
   * if many tags are declared.
   * 
   * <p>Should be called within the AbstractEquipment write lock.
   * 
   * <p>Should not throw the {@link IllegalAccessException} (only Tags can).
   * @param equipmentId of the (Sub)Equipment
   * @param properties containing the changed fields
   * @return ProcessChange used only for Equipment reconfiguration (not SubEquipment)
   * @throws IllegalAccessException not thrown for Equipment
   */
  protected List<ProcessChange> updateAbstractEquipment(final T abstractEquipment, final Properties properties) 
                                    throws IllegalAccessException {
    LOGGER.debug("Updating (Sub)Equipment " + abstractEquipment.getId());
    //TODO or not todo: warning: can still update commfault, alive and state tag id to non-existent tags (id is NOT checked and exceptions will be thrown!)
    
    //do not allow id changes! (they would not be applied in any case)
    if (properties.containsKey("id")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, 
          "Attempting to change the (sub)equipment id - this is not currently supported!");
    }    
  
    EquipmentConfigurationUpdate equipmentUpdate;
    try {    
      equipmentUpdate = (EquipmentConfigurationUpdate) commonEquipmentFacade.updateConfig(abstractEquipment, properties);
      configurableDAO.updateConfig(abstractEquipment);      
    } catch (Exception ex) {
      //if failure, remove equipment from cache; also clean Process, AliveTimer and CommFaultTag caches
      abstractEquipmentCache.remove(abstractEquipment.getId());      
      throw new UnexpectedRollbackException("Unexpected exception caught when updating an (Sub)Equipment configuration"
          + " - rolling back the DB changes and cleaning the cache", ex);
    }
    
    //create change event for DAQ layer
    Long processId = commonEquipmentFacade.getProcessForAbstractEquipment(abstractEquipment.getId()).getId();
    ArrayList<ProcessChange> processChanges = new ArrayList<ProcessChange>();
    processChanges.add(new ProcessChange(commonEquipmentFacade.getProcessForAbstractEquipment(abstractEquipment.getId()).getId(), equipmentUpdate));
    //if alive tags associated to equipment are changed and have an address, 
    //  inform DAQ also (use same changeId so these become sub-reports of the correct report)
    if (equipmentUpdate.getAliveTagId() != null) {
      ProcessChange processChange = controlTagConfigHandler.getCreateEvent(equipmentUpdate.getChangeId(), abstractEquipment.getAliveTagId(), abstractEquipment.getId(), processId);
      //null if this alive does not have an Address -> is not in list of DataTags on DAQ
      if (processChange != null) {
        processChanges.add(processChange);
      }      
    } 
    return processChanges;
  }
  
  /**
   * Removes the control tags for this equipment. Notice that if this fails, the
   * equipment object will still be removed: this is to prevent the situation of
   * not being able to remove the equipment because of the control tags (say if another
   * equipment is also using them by mistake) and not being able to remove the
   * control tags because of the equipment.
   * 
   * <p>Notice that in case of failure, only part of the control tags could remain; they
   * are removed in the following order: Alive tag, CommFaultTag, State tag.
   *  
   * @param abstractEquipment the AbstracEquipment to remove
   * @param equipmentReport for adding the subreports to
   */
  protected void removeEquipmentControlTags(final T abstractEquipment, final ConfigurationElementReport equipmentReport) {
    LOGGER.debug("Removing Equipment control tags.");
    Long aliveTagId = abstractEquipment.getAliveTagId();
    if (aliveTagId != null) {
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.CONTROLTAG, aliveTagId);
      equipmentReport.addSubReport(tagReport);
      controlTagConfigHandler.removeControlTag(aliveTagId, tagReport);      
    }    
    Long commTagId = abstractEquipment.getCommFaultTagId();
    if (commTagId != null) {
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.CONTROLTAG, commTagId);
      equipmentReport.addSubReport(tagReport);
      controlTagConfigHandler.removeControlTag(commTagId, tagReport);     
    }    
    Long stateTagId = abstractEquipment.getStateTagId();
    ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.CONTROLTAG, stateTagId);
    equipmentReport.addSubReport(tagReport);
    controlTagConfigHandler.removeControlTag(stateTagId, tagReport);    
  }
}
