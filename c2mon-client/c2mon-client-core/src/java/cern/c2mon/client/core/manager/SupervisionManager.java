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
import cern.c2mon.client.jms.JmsHealthListener;
import cern.c2mon.client.jms.JmsHealthMonitor;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.client.jms.SupervisionListener;
import cern.c2mon.shared.client.supervision.Heartbeat;
import cern.c2mon.shared.client.supervision.SupervisionEvent;

/**
 * The supervision manager allows registering listeners to get informed about
 * the connection state to the JMS brokers and the heartbeat of the C2MON server.
 * Furthermore it manages the supervision status information of the DAQ processes
 * and their equipment. Those information are only accessible through the
 * {@link CoreSupervisionManager} interface for other C2MON managers in the core API. 
 *
 * @author Matthias Braeger
 */
@Service
public class SupervisionManager implements CoreSupervisionManager, SupervisionListener, ConnectionListener, HeartbeatListener {

  /** Log4j logger instance */
  private static final Logger LOG = Logger.getLogger(SupervisionManager.class);
  
  /** Set to <code>true</code>, if the supervision  cache is correctly initialized */
  private volatile boolean c2monConnectionEstablished = false;
  
  /** <code>true</code>, if JmsProxy sent a <code>onConnection()</code> notification */
  private volatile boolean jmsConnectionUp = false;
  
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
  
  /** Reference to the <code>HeartbeatManager</code> singleton instance */
  private final HeartbeatListenerManager heartbeatManager;
  
  /** Monitors health of update processing */
  private final JmsHealthMonitor jmsHealthMonitor;
  
  @Autowired
  protected SupervisionManager(final JmsProxy pJmsProxy, final RequestHandler pRequestHandler, final HeartbeatListenerManager pHeartbeatManager,
                                final JmsHealthMonitor pJmsHealthMonitor) {
    jmsProxy = pJmsProxy;
    clientRequestHandler = pRequestHandler;
    heartbeatManager = pHeartbeatManager;
    jmsHealthMonitor = pJmsHealthMonitor;
  }
  
  /**
   * Called by Spring to initialize this service.
   */
  @PostConstruct
  private void init() {
    jmsProxy.registerConnectionListener(this);
    jmsProxy.registerSupervisionListener(this);
    heartbeatManager.addHeartbeatListener(this);
  }
  
  @Override
  public void addConnectionListener(final ConnectionListener pListener) {
    if (jmsConnectionUp) {
      pListener.onConnection();
    }
    else {
      pListener.onDisconnection();
    }
    jmsProxy.registerConnectionListener(pListener);
  }

  @Override
  public void addHeartbeatListener(final HeartbeatListener listener) {
    heartbeatManager.addHeartbeatListener(listener);
  }

  @Override
  public void removeHeartbeatListener(final HeartbeatListener listener) {
    heartbeatManager.removeHeartbeatListener(listener);
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
            fireEquipmentSupervisionUpdate(supervisionEvent);
            break;
          default:
            String errMsg = supervisionEvent.getEntity() + " SupervisionEvent is not supported by this client - not taking any action";
            LOG.debug("onSupervisionUpdate() - " + errMsg);            
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
          else if (!processEventCache.get(id).equals(supervisionEvent)) {
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
          else if (!equipmentEventCache.get(id).equals(supervisionEvent)) {
            equipmentEventCache.put(supervisionEvent.getEntityId(), supervisionEvent);
          }
          else {
            updated = false;
          }
          break;
        default:
          String errMsg = supervisionEvent.getEntity() + " SupervisionEvent is not supported by this client - ignoring the event";
          LOG.debug("updateEventCache() - " + errMsg);
          updated = false;
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
        Set<SupervisionListener> listeners = processSupervisionListeners.get(processId);
        if (listeners.add(listener)) {
          // Inform listener about the latest process event
          listener.onSupervisionUpdate(processEventCache.get(processId));
        }
      }
      
      for (Long equipmentId : equipmentIds) {
        if (!equipmentSupervisionListeners.containsKey(equipmentId)) {
          equipmentSupervisionListeners.put(equipmentId, new HashSet<SupervisionListener>());
        }
        Set<SupervisionListener> listeners = equipmentSupervisionListeners.get(equipmentId);
        if (listeners.add(listener)) {
          // Inform listener about the latest equipment event
          listener.onSupervisionUpdate(equipmentEventCache.get(equipmentId));
        }
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
    jmsConnectionUp = true;
    if (!c2monConnectionEstablished) {
      refreshSupervisionStatus();
    }
  }
  
  @Override
  public void refreshSupervisionStatus() {
    try {
      Collection<SupervisionEvent> allCurrentEvents = clientRequestHandler.getCurrentSupervisionStatus();
      for (SupervisionEvent event : allCurrentEvents) {
        onSupervisionUpdate(event);
      }
      c2monConnectionEstablished = true;
      LOG.info("refreshSupervisionStatus() - supervision event cache was successfully updated with " + allCurrentEvents.size() + " events.");
    }
    catch (Exception e) {
      LOG.error("refreshSupervisionStatus() - Could not initialize/update the supervision event cache. Reason: " + e.getMessage(), e);
      c2monConnectionEstablished = false;
    }
  }

  @Override
  public void onDisconnection() {
    jmsConnectionUp = false;
    c2monConnectionEstablished = false;
  }

  @Override
  public boolean isServerConnectionWorking() {
    return c2monConnectionEstablished;
  }

  @Override
  public void onHeartbeatExpired(Heartbeat pHeartbeat) {
    c2monConnectionEstablished = false;
  }

  @Override
  public void onHeartbeatReceived(Heartbeat pHeartbeat) {
    if (!c2monConnectionEstablished) {
      refreshSupervisionStatus();
    }
  }

  @Override
  public void onHeartbeatResumed(Heartbeat pHeartbeat) {
    if (!c2monConnectionEstablished) {
      refreshSupervisionStatus();
    }
  }

  @Override
  public void registerJmsHealthListener(JmsHealthListener jmsHealthListener) {
    jmsHealthMonitor.registerHealthListener(jmsHealthListener);
  }
}
