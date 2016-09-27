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

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.jms.JMSException;

import cern.c2mon.client.common.listener.BaseTagListener;

// TODO: use SourceDataTag or some basic tag from the client side client
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.AlarmExpressionService;
import cern.c2mon.client.core.jms.JmsProxy;
import cern.c2mon.client.core.jms.RequestHandler;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.rule.RuleFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("alarmExpressionService")
@Slf4j
public class AlarmExpressionServiceImpl implements AlarmExpressionService, BaseTagListener {

  /** Lock for accessing the <code>listeners</code> variable */
  private ReentrantReadWriteLock alarmListenersLock = new ReentrantReadWriteLock();

  /** List of subscribed alarm listeners */
  private final Set<BaseTagListener> alarmListeners = new HashSet<>();

  /** Reference to the <code>JmsProxy</code> singleton instance */
  private final JmsProxy jmsProxy;

  /** Provides methods for requesting tag information from the C2MON server */
  private final RequestHandler clientRequestHandler;

  /**
   * Default Constructor, used by Spring to instantiate the Singleton service
   *
   * @param jmsProxy       Used to register to the active alarm topic
   * @param requestHandler Provides methods for requesting tag information from the C2MON server
   */
  @Autowired
  protected AlarmExpressionServiceImpl(final JmsProxy jmsProxy,
                                       @Qualifier("coreRequestHandler") final RequestHandler requestHandler) {

    this.jmsProxy = jmsProxy;
    this.clientRequestHandler = requestHandler;
  }

  @Override
  public void addAlarmExpressionListener(final BaseTagListener listener) throws JMSException {
    alarmListenersLock.writeLock().lock();

    try {
      if (alarmListeners.size() == 0) {
        jmsProxy.registerAlarmExpressionListener(this);
      }

      log.debug("addAlarmListener() : adding alarm listener " + listener.getClass());
      alarmListeners.add(listener);
    } finally {
      alarmListenersLock.writeLock().unlock();
    }
  }

  @Override
  public void removeAlarmListener(final BaseTagListener listener) throws JMSException {
    alarmListenersLock.writeLock().lock();
    try {
      log.debug("removeAlarmListener() : removing alarm listener");

      if (alarmListeners.size() == 1) {
        jmsProxy.unregisterAlarmExpressionListener(this);
      }

      alarmListeners.remove(listener);
    } finally {
      alarmListenersLock.writeLock().unlock();
    }
  }

  @Override
  public Collection<Tag> getAlarmExpressions(final Collection<Long> tagIds) {
    List<Tag> result = new ArrayList<>();

    try {
      ClientDataTagImpl cdt;
      for(TagUpdate tagUpdate : clientRequestHandler.requestTags(tagIds)){
        cdt = new ClientDataTagImpl(tagUpdate.getId());
        cdt.update(tagUpdate);
      }
    } catch (JMSException e) {
      log.error("getAlarms() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    } catch (RuleFormatException e) {
      log.error("Failing to update a ClientDataTag", e);
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public Collection<AlarmValue> getAllActiveAlarms() {

    try {
      return clientRequestHandler.requestAllActiveAlarms();
    } catch (JMSException e) {
      log.error("getAllActiveAlarms() - JMS connection lost -> Could not retrieve missing tags from the C2MON server" +
          ".", e);
    }
    return new ArrayList<>();
  }

  @Override
  public void onUpdate(final Tag tag) {
    alarmListenersLock.readLock().lock();

    try {
      log.debug("onAlarmUpdate() -  received tag with an alarm update for tagId:" + tag.getId());
      notifyAlarmListeners(tag);
    } finally {
      alarmListenersLock.readLock().unlock();
    }
  }

  /**
   * Private method, notifies all listeners for a update.
   *
   * @param tag the updated Alarm
   */
  private void notifyAlarmListeners(final Tag tag) {
    log.debug("onAlarmUpdate() -  there is:" + alarmListeners.size() + " listeners waiting to be notified!");
    alarmListeners.stream().forEach(listener -> listener.onUpdate(tag));
  }
}
