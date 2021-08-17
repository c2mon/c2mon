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
package cern.c2mon.client.core.service.impl;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.jms.*;
import cern.c2mon.client.core.listener.HeartbeatListener;
import cern.c2mon.client.core.manager.HeartbeatListenerManager;
import cern.c2mon.client.core.service.CoreSupervisionService;
import cern.c2mon.shared.client.supervision.Heartbeat;
import cern.c2mon.shared.client.supervision.SupervisionEvent;

/**
 * The supervision manager allows registering listeners to get informed about
 * the connection state to the JMS brokers and the heartbeat of the C2MON
 * server. Furthermore it manages the supervision status information of the DAQ
 * processes and their equipment. Those information are only accessible through
 * the {@link CoreSupervisionService} interface for other C2MON managers in the
 * core API.
 *
 * @author Matthias Braeger
 */
@Slf4j
@Service
public class SupervisionServiceImpl implements CoreSupervisionService, SupervisionListener, ConnectionListener, HeartbeatListener {

  private static final String UNKNOWN = "UNKNOWN";

  /**
   * Set to <code>true</code>, if the supervision cache is correctly initialized
   */
  private volatile boolean c2monConnectionEstablished = false;

  /**
   * <code>true</code>, if JmsProxy sent a <code>onConnection()</code>
   * notification
   */
  private volatile boolean jmsConnectionUp = false;

  /** Lock for changes on the listeners maps */
  private final ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock();

  /** Lock for changes on the cache maps */
  private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();

  /**
   * Lookup table for all registered listeners on a given process supervision
   * event
   */
  private final Map<Long, Set<SupervisionListener>> processSupervisionListeners = new HashMap<>();

  /**
   * Lookup table for all registered listeners on a given equipment supervision
   * event
   */
  private final Map<Long, Set<SupervisionListener>> equipmentSupervisionListeners = new HashMap<>();

  /**
   * Lookup table for all registered listeners on a given sub equipment
   * supervision event
   */
  private final Map<Long, Set<SupervisionListener>> subEquipmentSupervisionListeners = new HashMap<>();

  /**
   * Map containing the latest process supervision events. The key of the map is
   * the process id.
   */
  private final Map<Long, SupervisionEvent> processEventCache = new HashMap<>();

  /**
   * Map containing the latest equipment supervision events. The key of the map
   * is the equipment id.
   */
  private final Map<Long, SupervisionEvent> equipmentEventCache = new HashMap<>();

  /**
   * Map containing the latest sub equipment supervision events. The key of the
   * map is the sub equipment id.
   */
  private final Map<Long, SupervisionEvent> subEquipmentEventCache = new HashMap<>();

  /** Reference to the <code>JmsProxy</code> singleton instance */
  private final JmsProxy jmsProxy;

  /** Reference to the <code>RequestHandler</code> singleton instance */
  private final RequestHandler clientRequestHandler;

  /** Reference to the <code>HeartbeatManager</code> singleton instance */
  private final HeartbeatListenerManager heartbeatManager;

  /** Monitors health of update processing */
  private final ClientHealthMonitor jmsHealthMonitor;

  @Autowired
  protected SupervisionServiceImpl(final JmsProxy jmsProxy, final RequestHandler coreRequestHandler, final HeartbeatListenerManager heartbeatManager,
                                   final ClientHealthMonitor clientHealthMonitor) {
    this.jmsProxy = jmsProxy;
    this.clientRequestHandler = coreRequestHandler;
    this.heartbeatManager = heartbeatManager;
    this.jmsHealthMonitor = clientHealthMonitor;
    this.jmsProxy.registerConnectionListener(this);
    this.jmsProxy.registerSupervisionListener(this);
    this.heartbeatManager.addHeartbeatListener(this);
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
        case SUBEQUIPMENT:
          fireSubEquipmentSupervisionUpdate(supervisionEvent);
          break;
        default:
          String errMsg = supervisionEvent.getEntity() + " SupervisionEvent is not supported by this client - not taking any action";
          log.debug("onSupervisionUpdate() - " + errMsg);
        }
      }
      finally {
        listenersLock.readLock().unlock();
      }
    }
  }

  /**
   * Inner method to update supervision event cache maps
   *
   * @param supervisionEvent The new event
   * @return <code>true</code>, if the cache was updated otherwise
   *         <code>false</code>
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
      case SUBEQUIPMENT:
        if (!subEquipmentEventCache.containsKey(id)) {
          subEquipmentEventCache.put(supervisionEvent.getEntityId(), supervisionEvent);
        }
        else if (!subEquipmentEventCache.get(id).equals(supervisionEvent)) {
          subEquipmentEventCache.put(supervisionEvent.getEntityId(), supervisionEvent);
        }
        else {
          updated = false;
        }
        break;
      default:
        String errMsg = supervisionEvent.getEntity() + " SupervisionEvent is not supported by this client - ignoring the event";
        log.debug("updateEventCache() - " + errMsg);
        updated = false;
      }
    }
    finally {
      cacheLock.writeLock().unlock();
    }

    return updated;
  }

  /**
   * Inner method to inform all subscribed listeners of the process supervision
   * event
   *
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
   * Inner method to inform all subscribed listeners of the equipment
   * supervision event
   *
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

  /**
   * Inner method to inform all subscribed listeners of the sub equipment
   * supervision event
   *
   * @param supervisionEvent The event
   */
  private void fireSubEquipmentSupervisionUpdate(final SupervisionEvent supervisionEvent) {
    Set<SupervisionListener> listeners = subEquipmentSupervisionListeners.get(supervisionEvent.getEntityId());
    if (listeners != null) {
      for (SupervisionListener listener : listeners) {
        listener.onSupervisionUpdate(supervisionEvent);
      }
    }
  }

  @Override
  public void addSupervisionListener(final SupervisionListener listener, final Collection<Long> processIds, final Collection<Long> equipmentIds,
      final Collection<Long> subEquipmentIds) {
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

      for (Long subEquipmentId : subEquipmentIds) {
        if (!subEquipmentSupervisionListeners.containsKey(subEquipmentId)) {
          subEquipmentSupervisionListeners.put(subEquipmentId, new HashSet<SupervisionListener>());
        }
        Set<SupervisionListener> listeners = subEquipmentSupervisionListeners.get(subEquipmentId);
        if (listeners.add(listener)) {
          // Inform listener about the latest sub equipment event
          listener.onSupervisionUpdate(subEquipmentEventCache.get(subEquipmentId));
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

      for (Collection<SupervisionListener> listeners : subEquipmentSupervisionListeners.values()) {
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
  
  private void clearSupervisionCaches() {
    processEventCache.clear();
    equipmentEventCache.clear();
    subEquipmentSupervisionListeners.clear();
  }
  
  private void refreshSupervisionStatusCaches() {
    try {
      Collection<SupervisionEvent> allCurrentEvents = clientRequestHandler.getCurrentSupervisionStatus();
      clearSupervisionCaches();
      for (SupervisionEvent event : allCurrentEvents) {
        updateEventCache(event);
      }
      c2monConnectionEstablished = true;
      log.info("Supervision event cache was successfully updated with " + allCurrentEvents.size() + " events.");
    }
    catch (Exception e) {
      log.error("Could not initialize/update the supervision event cache. Reason: " + e.getMessage(), e);
      c2monConnectionEstablished = false;
    }
  }

  @Override
  public void refreshSupervisionStatus() {
    try {
      Collection<SupervisionEvent> allCurrentEvents = clientRequestHandler.getCurrentSupervisionStatus();
      clearSupervisionCaches();
      for (SupervisionEvent event : allCurrentEvents) {
        onSupervisionUpdate(event);
      }
      c2monConnectionEstablished = true;
      log.info("Supervision event cache was successfully updated with " + allCurrentEvents.size() + " events.");
    }
    catch (Exception e) {
      log.error("Could not initialize/update the supervision event cache. Reason: " + e.getMessage(), e);
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
  public void addClientHealthListener(ClientHealthListener clientHealthListener) {
    jmsHealthMonitor.addHealthListener(clientHealthListener);
  }

  @Override
  public String getProcessName(Long processId) {
    SupervisionEvent process = this.processEventCache.get(processId);
    if (process != null) {
      return process.getName();
    }
    return UNKNOWN;
  }

  @Override
  public String getEquipmentName(Long equipmentId) {
    SupervisionEvent equipment = this.equipmentEventCache.get(equipmentId);
    if (equipment != null) {
      return equipment.getName();
    }
    return UNKNOWN;
  }

  @Override
  public String getSubEquipmentName(Long subEquipmentId) {
    SupervisionEvent subEquipment = this.subEquipmentEventCache.get(subEquipmentId);
    if (subEquipment != null) {
      return subEquipment.getName();
    }
    return UNKNOWN;
  }

  @Override
  public Collection<String> getAllProcessNames() {
    refreshSupervisionStatusCaches();
    return processEventCache.values().stream().map(SupervisionEvent::getName).collect(Collectors.toList());
  }

  @Override
  public Collection<String> getAllEquipmentNames() {
    refreshSupervisionStatusCaches();
    return equipmentEventCache.values().stream().map(SupervisionEvent::getName).collect(Collectors.toList());
  }

  @Override
  public Collection<String> getAllSubEquipmentNames() {
    refreshSupervisionStatusCaches();
    return subEquipmentEventCache.values().stream().map(SupervisionEvent::getName).collect(Collectors.toList());
  }

  @Override
  public SupervisionEvent getProcessSupervisionEvent(Long processId) {
    return this.getProcessSupervisionEvent(processId);
  }

  @Override
  public SupervisionEvent getEquipmentSupervisionEvent(Long equipmentId) {
    return this.getEquipmentSupervisionEvent(equipmentId);
  }

  @Override
  public SupervisionEvent getSubEquipmentSupervisionEvent(Long subEquipmentId) {
    return this.subEquipmentEventCache.get(subEquipmentId);
  }
}
