/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.client.core.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.listener.HeartbeatListener;
import cern.c2mon.client.jms.ConnectionListener;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.client.jms.SupervisionListener;
import cern.c2mon.shared.client.supervision.SupervisionEvent;

@Service
public class SupervisionManager implements CoreSupervisionManager, SupervisionListener, ConnectionListener {

  /** Log4j logger instance */
  private static final Logger LOG = Logger.getLogger(SupervisionManager.class);
  
  /** Set to <code>true</code>, if the supervision  cache is correctly initialized */
  private boolean c2monConnectionEstablished = false;
  
  /** Lock for changes on the listeners maps */
  private final ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock();
  
  /** Lock for changes on the cache maps */
  private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
  
  /** Lookup table for all registered listeners on a given process supervision event */
  private final Map<Long, Set<SupervisionListener>> processSupervisionListeners = 
    new HashMap<Long, Set<SupervisionListener>>();
  
  /** Lookup table for all registered listeners on a given equipment supervision event */
  private final Map<Long, Set<SupervisionListener>> equipmentSupervisionListeners = 
    new HashMap<Long, Set<SupervisionListener>>();
  
  /** Map containing the latest process supervision events. The key of the map is the process id. */
  private final Map<Long, SupervisionEvent> processEventCache = 
    new HashMap<Long, SupervisionEvent>();
  
  /** Map containing the latest equipment supervision events. The key of the map is the equipment id. */
  private final Map<Long, SupervisionEvent> equipmentEventCache = 
    new HashMap<Long, SupervisionEvent>();
  
  /** Reference to the <code>JmsProxy</code> singleton instance */
  private final JmsProxy jmsProxy;
  
  /** Reference to the <code>RequestHandler</code> singleton instance */
  private final RequestHandler clientRequestHandler;
  
  @Autowired
  protected SupervisionManager(final JmsProxy pJmsProxy, final RequestHandler pRequestHandler) {
    jmsProxy = pJmsProxy;
    clientRequestHandler = pRequestHandler;
  }
  
  @PostConstruct
  private void init() {
    jmsProxy.registerConnectionListener(this);
    jmsProxy.registerSupervisionListener(this);
  }
  
  @Override
  public void addConnectionListener(final ConnectionListener pListener) {
    jmsProxy.registerConnectionListener(pListener);
  }

  @Override
  public void addHeartbeatListener(final HeartbeatListener pListener) {
    // TODO: Implement method!
  }

  @Override
  public void removeHeartbeatListener(final HeartbeatListener pListener) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public synchronized void onSupervisionUpdate(final SupervisionEvent supervisionEvent) {
    boolean updated = updateEventCache(supervisionEvent);
    
    if (updated) {
      listenersLock.readLock().lock();
      try {
        switch (supervisionEvent.getEntity()) {
          case PROCESS:
            fireProcessSupervisionUpdate(supervisionEvent);
            break;
          case EQUIPMENT:
            equipmentEventCache.put(supervisionEvent.getEntityId(), supervisionEvent);
            fireEquipmentSupervisionUpdate(supervisionEvent);
            break;
          default:
            String errMsg = supervisionEvent.getEntity() + " SupervisionEvent is not supported by this client. Needs restarting!";
            LOG.fatal("onSupervisionUpdate() - " + errMsg);
            throw new RuntimeException(errMsg);
        }
      }
      finally {
        listenersLock.readLock().unlock();
      }
    }
  }
  
  /**
   * Inner method to update supervision event cache maps
   * @param supervisionEvent The new event
   * @return <code>true</code>, if the cache was updated otherwise <code>false</code>
   */
  private boolean updateEventCache(final SupervisionEvent supervisionEvent) {
    final Long id = supervisionEvent.getEntityId();
    boolean updated = true;
    cacheLock.writeLock().lock();
    try {
      switch (supervisionEvent.getEntity()) {
        case PROCESS:
          if (!processEventCache.containsKey(id)) {
            processEventCache.put(supervisionEvent.getEntityId(), supervisionEvent);
          }
          else if (processEventCache.get(id).getEventTime().before(supervisionEvent.getEventTime())) {
            processEventCache.put(supervisionEvent.getEntityId(), supervisionEvent);
          }
          else {
            updated = false;
          }
          break;
        case EQUIPMENT:
          if (!equipmentEventCache.containsKey(id)) {
            equipmentEventCache.put(supervisionEvent.getEntityId(), supervisionEvent);
          }
          else if (equipmentEventCache.get(id).getEventTime().before(supervisionEvent.getEventTime())) {
            equipmentEventCache.put(supervisionEvent.getEntityId(), supervisionEvent);
          }
          else {
            updated = false;
          }
          break;
        default:
          String errMsg = supervisionEvent.getEntity() + " SupervisionEvent is not supported by this client. Needs restarting!";
          LOG.fatal("updateEventCache() - " + errMsg);
          throw new RuntimeException(errMsg);
      }
    }
    finally {
      cacheLock.writeLock().unlock();
    }
    
    return updated;
  }
  
  /**
   * Inner method to inform all subscribed listeners of the process supervision event
   * @param supervisionEvent The event
   */
  private void fireProcessSupervisionUpdate(final SupervisionEvent supervisionEvent) {
    Set<SupervisionListener> listeners = processSupervisionListeners.get(supervisionEvent.getEntityId());
    if (listeners != null) {
      for (SupervisionListener listener : listeners) {
        listener.onSupervisionUpdate(supervisionEvent);
      }
    }
  }
  
  /**
   * Inner method to inform all subscribed listeners of the equipment supervision event
   * @param supervisionEvent The event
   */
  private void fireEquipmentSupervisionUpdate(final SupervisionEvent supervisionEvent) {
    Set<SupervisionListener> listeners = equipmentSupervisionListeners.get(supervisionEvent.getEntityId());
    if (listeners != null) {
      for (SupervisionListener listener : listeners) {
        listener.onSupervisionUpdate(supervisionEvent);
      }
    }
  }

  @Override
  public void addSupervisionListener(final SupervisionListener listener, final Collection<Long> processIds, final Collection<Long> equipmentIds) {
    listenersLock.writeLock().lock();
    cacheLock.readLock().lock();
    try {
      for (Long processId : processIds) {
        if (!processSupervisionListeners.containsKey(processId)) {
          processSupervisionListeners.put(processId, new HashSet<SupervisionListener>());
        }
        Collection<SupervisionListener> listeners = processSupervisionListeners.get(processId);
        listeners.add(listener);
        
        // Inform listener about the latest process event
        listener.onSupervisionUpdate(processEventCache.get(processId));
      }
      
      for (Long equipmentId : equipmentIds) {
        if (!equipmentSupervisionListeners.containsKey(equipmentId)) {
          equipmentSupervisionListeners.put(equipmentId, new HashSet<SupervisionListener>());
        }
        Collection<SupervisionListener> listeners = equipmentSupervisionListeners.get(equipmentId);
        listeners.add(listener);
        
        // Inform listener about the latest process event
        listener.onSupervisionUpdate(equipmentEventCache.get(equipmentId));
      }
    }
    finally {
      cacheLock.readLock().unlock();
      listenersLock.writeLock().unlock();
    }
  }

  @Override
  public void removeSupervisionListener(final SupervisionListener listener) {
    listenersLock.writeLock().lock();
    try {
      for (Collection<SupervisionListener> listeners : processSupervisionListeners.values()) {
        listeners.remove(listener);
      }
      
      for (Collection<SupervisionListener> listeners : equipmentSupervisionListeners.values()) {
        listeners.remove(listener);
      }
    }
    finally {
      listenersLock.writeLock().unlock();
    }
  }

  @Override
  public void onConnection() {
    try {
      Collection<SupervisionEvent> allCurrentEvents = clientRequestHandler.getCurrentSupervisionStatus();
      for (SupervisionEvent event : allCurrentEvents) {
        onSupervisionUpdate(event);
      }
      c2monConnectionEstablished = true;
      LOG.info("onConnection() - supervision event cache was successfully updated with " + allCurrentEvents.size() + " events.");
    }
    catch (Exception e) {
      LOG.error("onConnection() - Could not initialize/update the supervision event cache. Reason: " + e.getMessage());
      c2monConnectionEstablished = false;
    }
  }

  @Override
  public void onDisconnection() {
    c2monConnectionEstablished = false;
  }

  @Override
  public boolean isServerConnectionWorking() {
    return c2monConnectionEstablished;
  }
}
