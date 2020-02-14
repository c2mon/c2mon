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
package cern.c2mon.server.client.publish;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.republisher.Publisher;
import cern.c2mon.server.common.republisher.Republisher;
import cern.c2mon.server.common.republisher.RepublisherFactory;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.util.jms.JmsSender;
import cern.c2mon.shared.util.json.GsonFactory;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.jms.JmsException;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Publishes active alarms to the C2MON client applications on the
 * alarm publication topic, specified using the property
 * jms.client.alarm.topic
 *
 * <p>Will attempt re-publication of alarms if JMS connection fails.
 *
 *
 * @author Manos, Mark Brightwell
 *
 */
@Slf4j
@Service
@ManagedResource(description = "Bean publishing Alarm updates to the clients")
public class AlarmPublisher implements SmartLifecycle, Publisher<AlarmValue>  {

  /** Bean providing for sending JMS messages and waiting for a response */
  private final JmsSender jmsSender;

  private final C2monCache<Alarm> alarmCache;

  /** Reference to the tag location service to check whether a tag exists */
  private final TagCacheCollection unifiedTagCacheFacade;

  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();

  /** Contains re-publication logic */
  private Republisher<AlarmValue> republisher;

  /** Lifecycle flag */
  private volatile boolean running = false;

  /**
   * Default Constructor
   * @param jmsSender Used for sending JMS messages and waiting for a response.
   * @param alarmCache Used to register to Alarm updates.
   * @param unifiedTagCacheFacade Reference to the tag location service singleton.
   * Used to add tag information to the AlarmValue object.
   */
  @Autowired
  public AlarmPublisher(@Qualifier("alarmTopicPublisher") final JmsSender jmsSender
      , final C2monCache<Alarm> alarmCache
      , final TagCacheCollection unifiedTagCacheFacade) {

    this.jmsSender = jmsSender;
    this.alarmCache = alarmCache;
    this.unifiedTagCacheFacade = unifiedTagCacheFacade;
    republisher = RepublisherFactory.createRepublisher(this, "Alarm");
  }

  /**
   * Registering this listener to alarms.
   */
  @PostConstruct
  void init() {
    alarmCache.getCacheListenerManager().registerListener(this::notifyElementUpdated
    , CacheEvent.UPDATE_ACCEPTED, CacheEvent.CONFIRM_STATUS);
  }

  /**
   * Generates for every alarm update an <code>AlarmValue</code>
   * object which is then sent as serialized GSON message trough the
   * JMS client topic.
   * @param alarm the updated alarm
   */
  public void notifyElementUpdated(final Alarm alarm) {

    Long tagId = alarm.getTagId();
    AlarmValue alarmValue = null;

    if (unifiedTagCacheFacade.containsKey(tagId)) {
      Tag tag = unifiedTagCacheFacade.get(tagId);
      alarmValue = (TransferObjectFactory.createAlarmValue(alarm, tag));
    }
    else {
      log.warn("notifyElementUpdated() - unrecognized Tag with id " + tagId);
      alarmValue = (TransferObjectFactory.createAlarmValue(alarm));
    }
    try {
      publish(alarmValue);
    } catch (JmsException e) {
      log.error("Error publishing alarm to clients - submitting for republication. Alarm id is " + alarmValue.getId(), e);
      republisher.publicationFailed(alarmValue);
    }
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable runnable) {
    stop();
    runnable.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    log.debug("Starting Alarm publisher");
    running = true;
    republisher.start();
  }

  @Override
  public void stop() {
    log.debug("Stopping Alarm publisher");
    republisher.stop();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;
  }

  @Override
  public void publish(final AlarmValue alarmValue) {
    String jsonAlarm = GSON.toJson(alarmValue);
    log.debug("Publishing alarm: " + jsonAlarm);
    jmsSender.send(jsonAlarm);
  }

  /**
   * @return the total number of failed publications since the publisher start
   */
  @ManagedOperation(description = "Returns the total number of failed alarm publication attempts since the application started")
  public long getNumberFailedPublications() {
    return republisher.getNumberFailedPublications();
  }

  /**
   * @return the number of current tag updates awaiting publication to the clients
   */
  @ManagedOperation(description = "Returns the current number of alarms awaiting re-publication (should be 0 in normal operation)")
  public int getSizeUnpublishedList() {
    return republisher.getSizeUnpublishedList();
  }
}
