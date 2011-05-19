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
package cern.c2mon.server.configuration.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.cache.AliveTimerCache;
import cern.tim.server.cache.AliveTimerFacade;
import cern.tim.server.cache.CommFaultTagCache;
import cern.tim.server.cache.CommFaultTagFacade;
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
  private AliveTimerFacade aliveTimerFacade;
  
  /**
   * Facade to CommFaultTag.
   */
  private CommFaultTagFacade commFaultTagFacade;
  
  /**
   * Constructor.
   * @param controlTagConfigHandler the ConfigHandler for ControlTags
   * @param commonEquipmentFacade the Facade bean for the (Sub)Equipment
   * @param abstractEquipmentCache the Cache bean for the (Sub)Equipment
   * @param configurableDAO the DAO for the (Sub)Equipment
   * @param aliveTimerFacade the AliveTimer facade bean
   * @param commFaultTagFacade
   */
  public AbstractEquipmentConfigHandler(
      final ControlTagConfigHandler controlTagConfigHandler,     
      final CommonEquipmentFacade<T> commonEquipmentFacade,
      TimCache<T> abstractEquipmentCache, ConfigurableDAO<T> configurableDAO,
      AliveTimerFacade aliveTimerFacade, CommFaultTagFacade commFaultTagFacade) {
    super();
    this.controlTagConfigHandler = controlTagConfigHandler;
    this.commonEquipmentFacade = commonEquipmentFacade;
    this.abstractEquipmentCache = abstractEquipmentCache;
    this.configurableDAO = configurableDAO;
    this.aliveTimerFacade = aliveTimerFacade;
    this.commFaultTagFacade = commFaultTagFacade;
  }
  
  /**
   * Creates the abstract equipment, puts it in the DB and then loads it into the cache (in that order).
   * The AliveTimer and CommFaultTag are then generated in their respective caches.
   * @param element contains the creation detais
   * @return the generated AbstractEquipment object
   * @throws IllegalAccessException should not be thrown here (inherited at interface from Tag creation).
   */
  public T createAbstractEquipment(final ConfigurationElement element) throws IllegalAccessException {
    T abstractEquipment = commonEquipmentFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
    configurableDAO.insert(abstractEquipment);
    //TODO necessary to use DB loading or not?? to check...
    abstractEquipmentCache.putQuiet(abstractEquipment);    
    aliveTimerFacade.generateFromEquipment(abstractEquipment); //TODO same in process...
    commFaultTagFacade.generateFromEquipment(abstractEquipment);
    return abstractEquipment;
  }

  /**
   * Can be called directly for both Equipment and SubEquipment configuration updates.
   * 
   * <p>Should not throw the {@link IllegalAccessException} (only Tags can).
   * @param equipmentId of the (Sub)Equipment
   * @param properties containing the changed fields
   * @return ProcessChange used only for Equipment reconfiguration (not SubEquipment)
   * @throws IllegalAccessException not thrown for Equipment
   */
  public List<ProcessChange> updateAbstractEquipment(final Long equipmentId, final Properties properties) 
                                    throws IllegalAccessException {
    //TODO or not todo: warning: can still update commfault, alive and state tag id to non-existent tags (id is NOT checked and exceptions will be thrown!) 
    //do not allow id changes! (they would not be applied in any case)
    if (properties.containsKey("id")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, 
          "Attempting to change the (sub)equipment id - this is not currently supported!");
    }    
    T abstractEquipment = abstractEquipmentCache.get(equipmentId);
    EquipmentConfigurationUpdate equipmentUpdate;
    try {
      abstractEquipment.getWriteLock().lock();
      equipmentUpdate = (EquipmentConfigurationUpdate) commonEquipmentFacade.updateConfig(abstractEquipment, properties);
      configurableDAO.updateConfig(abstractEquipment);      
    } finally {
      abstractEquipment.getWriteLock().unlock();     
    }   
    Long processId = commonEquipmentFacade.getProcessForAbstractEquipment(equipmentId).getId();
    ArrayList<ProcessChange> processChanges = new ArrayList<ProcessChange>();
    processChanges.add(new ProcessChange(commonEquipmentFacade.getProcessForAbstractEquipment(equipmentId).getId(), equipmentUpdate));
    //if alive tags associated to equipment are changed and have an address, 
    //  inform DAQ also (use same changeId so these become sub-reports of the correct report)
    if (equipmentUpdate.getAliveTagId() != null) {
      ProcessChange processChange = controlTagConfigHandler.getCreateEvent(equipmentUpdate.getChangeId(), abstractEquipment.getAliveTagId(), equipmentId, processId);
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
