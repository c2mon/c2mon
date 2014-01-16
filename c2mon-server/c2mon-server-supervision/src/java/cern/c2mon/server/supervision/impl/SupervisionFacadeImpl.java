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
package cern.c2mon.server.supervision.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.ControlTagFacade;
import cern.c2mon.server.cache.DataTagFacade;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessFacade;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.SubEquipmentFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.server.supervision.SupervisionFacade;
import cern.c2mon.server.supervision.SupervisionNotifier;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

/**
 * Implementation of the SupervisionFacade.
 * 
 * @author Mark Brightwell
 *
 */
@Service
@ManagedResource(objectName="cern.c2mon:name=supervisionFacade")
public class SupervisionFacadeImpl implements SupervisionFacade {

  /**
   * Class logger.
   */
  public static final Logger LOGGER = Logger.getLogger(SupervisionFacadeImpl.class);
  
  /**
   * Delay at server start-up before current supervision status saved to the DB.
   */
  public static final int INITIAL_LOGGING_DELAY = 120;
  
  /**
   * Reference to Process cache.
   */
  private ProcessCache processCache; 
  
  /**
   * Reference to Process facade.
   */
  private ProcessFacade processFacade;
  
  /**
   * Reference to Equipment cache.
   */
  private EquipmentCache equipmentCache;
  
  /**
   * Reference to Equipment facade.
   */
  private EquipmentFacade equipmentFacade;
  
  /**
   * Reference to SubEquipment cache.
   */
  private SubEquipmentCache subEquipmentCache;
  
  /**
   * Reference to SubEquipment facade.
   */
  private SubEquipmentFacade subEquipmentFacade;
  
  /**
   * Ref to bean notifying supervision listeners.
   */
  private SupervisionNotifier supervisionNotifier;
  
  /**
   * For accessing state tags.
   */
  private ControlTagFacade controlTagFacade;
 
  /**
   * Management value tracing the number of requests for the supervision status
   * that are waiting for a response.
   */
  private volatile AtomicInteger pendingRequests = new AtomicInteger(0);
  
  /**
   * Autowired constructor.
   * @param processCache process cache bean
   * @param processFacade process facade
   * @param equipmentCache equipment cache
   * @param equipmentFacade equipment facade
   * @param subEquipmentCache subequipment cache
   * @param subEquipmentFacade subequipment facade
   * @param dataTagFacade datatag facade
   * @param controlTagCache controltag cache
   */
  @Autowired
  public SupervisionFacadeImpl(final ProcessCache processCache, final ProcessFacade processFacade, final EquipmentCache equipmentCache,
      final EquipmentFacade equipmentFacade, final SubEquipmentCache subEquipmentCache, final SubEquipmentFacade subEquipmentFacade,
      final SupervisionNotifier supervisionNotifier, final ControlTagFacade controlTagFacade) {
    super();
    this.processCache = processCache;
    this.processFacade = processFacade;
    this.equipmentCache = equipmentCache;
    this.equipmentFacade = equipmentFacade;
    this.subEquipmentCache = subEquipmentCache;
    this.subEquipmentFacade = subEquipmentFacade;
    this.supervisionNotifier = supervisionNotifier;
    this.controlTagFacade = controlTagFacade; 
  }

  @Override
  public Collection<SupervisionEvent> getAllSupervisionStates() {
    try {
      pendingRequests.getAndIncrement();
      Collection<SupervisionEvent> supervisionCollection = new ArrayList<SupervisionEvent>();
      for (Long key : processCache.getKeys()) { //is copy of keys
        supervisionCollection.add(processFacade.getSupervisionStatus(key));
      }
      for (Long key : equipmentCache.getKeys()) {
        supervisionCollection.add(equipmentFacade.getSupervisionStatus(key));
      }
      for (Long key : subEquipmentCache.getKeys()) {
        supervisionCollection.add(subEquipmentFacade.getSupervisionStatus(key));
      }
      return supervisionCollection;   
    } finally {
      pendingRequests.getAndDecrement();
    }    
  }

  /**
   * Notifies all listeners of all supervised cache objects (so Process, Equipment,
   * SubEquipments) with the current object. In particular the SupervisionNotifier
   * is called.
   * 
   * <p>This is used to refresh all listeners with the latest values for supervision 
   * purposes (in case of a previous server failure when some may not have been logged to DB).
   */
  private void notifyAllSupervisedCachesOfUpdate() {
    for (Long key : processCache.getKeys()) {
      processFacade.refreshAndnotifyCurrentSupervisionStatus(key);
    }
    for (Long key : equipmentCache.getKeys()) {
      equipmentFacade.refreshAndnotifyCurrentSupervisionStatus(key);
    }
    for (Long key : subEquipmentCache.getKeys()) {
      subEquipmentFacade.refreshAndnotifyCurrentSupervisionStatus(key);
    }
  }
  
  @Override
  public void refreshStateTags() {
    Timestamp refreshTime = new Timestamp(System.currentTimeMillis());
    for (Long key : processCache.getKeys()) {
      refreshStateTag(processCache.get(key), refreshTime);            
    }
    for (Long key : equipmentCache.getKeys()) {
      refreshStateTag(equipmentCache.get(key), refreshTime);
    }
    for (Long key : subEquipmentCache.getKeys()) {
      refreshStateTag(subEquipmentCache.get(key), refreshTime);
    }    
  }
  
  /**
   * Refreshes the state tag, using the current supervision status.
   * Will only update the status tags in the cache if they have actually changed.
   * @param supervised supervised object
   */
  private void refreshStateTag(final Supervised supervised, final Timestamp refreshTime) {
    try {
      Long stateTagId;
      String message;
      SupervisionStatus status;
     
      stateTagId = supervised.getStateTagId();
      message = supervised.getStatusDescription();
      status = supervised.getSupervisionStatus();                   
       
      controlTagFacade.updateAndValidate(stateTagId, status.toString(), message, refreshTime);
    } catch (Exception e) {
      LOGGER.error("Error while refreshing state tag for " + supervised.getSupervisionEntity() 
          + " " + supervised.getId() + " - unable to refresh this tag.", e);
    }    
  }

  /**
   * For management purposes.
   * Updates the supervision timestamp of all Processes and (Sub)Equipments, notifying
   * all listeners of the change (republishing supervision events also and re-evaluating
   * all alarms); takes some minutes to refresh all alarms.
   */
  @ManagedOperation(description="Refresh supervision timestamps of Processes/(Sub)Equipments and notify all listeners.")
  @Override
  public void refreshAllSupervisionStatus() {
    notifyAllSupervisedCachesOfUpdate();
  }
  
  /**
   * For management purposes.
   * @return the number of supervison status requests pending
   */
  @ManagedAttribute(description="Number of pending supervison status requests.")
  public int getPendingRequests() {
    return pendingRequests.intValue();
  }
  
}
