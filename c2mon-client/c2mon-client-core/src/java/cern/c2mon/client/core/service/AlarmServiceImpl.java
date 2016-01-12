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
package cern.c2mon.client.core.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.AlarmService;
import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.alarm.AlarmValue;
import lombok.extern.slf4j.Slf4j;

@Service @Slf4j
public class AlarmServiceImpl implements AlarmService, AlarmListener {
  
  /** Lock for accessing the <code>listeners</code> variable */
  private ReentrantReadWriteLock alarmListenersLock = new ReentrantReadWriteLock();

  /** List of subscribed alarm listeners */
  private final Set<AlarmListener> alarmListeners = new HashSet<AlarmListener>();
  
  /** Reference to the <code>JmsProxy</code> singleton instance */
  private final JmsProxy jmsProxy;
  
  /** Provides methods for requesting tag information from the C2MON server */
  private final RequestHandler clientRequestHandler;
  
  /**
   * Default Constructor, used by Spring to instantiate the Singleton service
   *
   * @param jmsProxy Used to register to the active alarm topic
   * @param requestHandler Provides methods for requesting tag information from the C2MON server
   */
  @Autowired
  protected AlarmServiceImpl(final JmsProxy jmsProxy,
      final RequestHandler requestHandler) {

    this.jmsProxy = jmsProxy;
    this.clientRequestHandler = requestHandler;
  }
  
  @Override
  public void addAlarmListener(final AlarmListener listener) throws JMSException {
    alarmListenersLock.writeLock().lock();

    try {
      if (alarmListeners.size() == 0) {
        jmsProxy.registerAlarmListener(this);
      }

      log.debug("addAlarmListener() : adding alarm listener " + listener.getClass());
      alarmListeners.add(listener);
    } finally {
      alarmListenersLock.writeLock().unlock();
    }
  }

  @Override
  public void removeAlarmListener(final AlarmListener listener) throws JMSException {
    alarmListenersLock.writeLock().lock();
    try {
      log.debug("removeAlarmListener() : removing alarm listener");

      if (alarmListeners.size() == 1) {
        jmsProxy.unregisterAlarmListener(this);
      }

      alarmListeners.remove(listener);
    } finally {
      alarmListenersLock.writeLock().unlock();
    }
  }
  
  @Override
  public Collection<AlarmValue> getAlarms(final Collection<Long> alarmIds) {

    try {
      return clientRequestHandler.requestAlarms(alarmIds);
    } catch (JMSException e) {
      log.error("getAlarms() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    }
    return new ArrayList<AlarmValue>();
  }
  
  @Override
  public Collection<AlarmValue> getAllActiveAlarms() {

    try {
      return clientRequestHandler.requestAllActiveAlarms();
    } catch (JMSException e) {
      log.error("getAllActiveAlarms() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    }
    return new ArrayList<AlarmValue>();
  }
  
  @Override
  public void onAlarmUpdate(final AlarmValue alarm) {
    alarmListenersLock.readLock().lock();

    try {
      log.debug("onAlarmUpdate() -  received alarm update for alarmId:" + alarm.getId());
      notifyAlarmListeners(alarm);
    }
    finally {
      alarmListenersLock.readLock().unlock();
    }
  }
  
  /**
   * Private method, notifies all listeners for an alarmUpdate.
   * @param alarm the updated Alarm
   */
  private void notifyAlarmListeners(final AlarmValue alarm) {

    log.debug("onAlarmUpdate() -  there is:" + alarmListeners.size()
        + " listeners waiting to be notified!");

    for (AlarmListener listener : alarmListeners) {

      listener.onAlarmUpdate(alarm);
    }
  }
}
