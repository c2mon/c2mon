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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.client.config.ClientProperties;
import cern.c2mon.shared.client.serializer.TransferTagSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.alarm.AlarmAggregator;
import cern.c2mon.server.alarm.AlarmAggregatorListener;
import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.server.common.republisher.Publisher;
import cern.c2mon.server.common.republisher.Republisher;
import cern.c2mon.server.common.republisher.RepublisherFactory;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.configuration.ConfigurationUpdate;
import cern.c2mon.server.configuration.ConfigurationUpdateListener;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import cern.c2mon.shared.util.jms.JmsSender;

/**
 * This class implements the <code>AlarmAggregatorListener</code>
 * interface for sending tag value updates to the tag JMS destination
 * topics. The update information is transmitted as GSON message
 * with the <code>TransferTagValue</code> class.
 *
 * This class implements the <code>ConfigurationUpdateListener</code>
 * interface for sending configuration updates to the tag JMS destination
 * topics. The update information is transmitted as GSON message
 * with the <code>TransferTag</code> class.
 *
 * @author Matthias Braeger, Mark Brightwell, Ignacio Vilches
 *
 * @see AlarmAggregatorListener
 * @see ConfigurationUpdateListener
 * @see TagValueUpdate
 */
@Service
@ManagedResource(description = "Bean publishing tag updates to the clients")
public class TagValuePublisher implements C2monCacheListener<Tag>, ConfigurationUpdateListener, Publisher<Tag> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TagValuePublisher.class);

  /** Bean providing for sending JMS messages and waiting for a response */
  private final JmsSender jmsSender;

  /** Listens for Configuration changes */
  private final ConfigurationUpdate configurationUpdate;

  /** Listens for Tag updates, evaluates all associated alarms and passes the result */
  @Deprecated
  private final AlarmAggregator alarmAggregator;

  /**
   * Used to register the aggregator as Tag update listener.
   */
  private final CacheRegistrationService cacheRegistrationService;

  /** Contains re-publication logic */
  private Republisher<Tag> republisher;

  /** Time between republicaton attempts */
  private int republicationDelay;

  /**
   * Reference to the tag facade gateway to retrieve a tag copies with the
   * associated alarms
   */
  private final TagFacadeGateway tagFacadeGateway;

  /** Reference to the tag location service */
  private TagLocationService tagLocationService;

  /** Used to determine, whether a given tag is an AliveTag */
  private AliveTimerFacade aliveTimerFacade;

  private ClientProperties properties;

  /**
   * Default Constructor
   * @param jmsSender Used for sending JMS messages and waiting for a response
   * @param aliveTimerFacade Used to determine, whether a given tag is an AliveTag
   * @param alarmAggregator Used to register this <code>AlarmAggregatorListener</code>
   * @param configurationUpdate Used to register this <code>ConfigurationUpdateListener</code>
   * @param pTagFacadeGateway Reference to the tag facade gateway singleton
   * @param tagLocationService Reference to the tag location service
   */
  @Autowired
  public TagValuePublisher(@Qualifier("clientTopicPublisher") final JmsSender jmsSender,
                           final AlarmAggregator alarmAggregator,
                           final AliveTimerFacade aliveTimerFacade,
                           final ConfigurationUpdate configurationUpdate,
                           final TagFacadeGateway pTagFacadeGateway,
                           final TagLocationService tagLocationService,
                           final ClientProperties properties,
                           final CacheRegistrationService cacheRegistrationService) {
    this.aliveTimerFacade = aliveTimerFacade;
    this.jmsSender = jmsSender;
    this.alarmAggregator = alarmAggregator;
    this.configurationUpdate = configurationUpdate;
    this.tagFacadeGateway = pTagFacadeGateway;
    this.tagLocationService = tagLocationService;
    this.republisher = RepublisherFactory.createRepublisher(this, "Tag");
    this.properties = properties;
    this.cacheRegistrationService = cacheRegistrationService;
  }

  /**
   * Init method registering this listener to the <code>AlarmAggregator</code>.
   */
  @PostConstruct
  public void init() {
    LOGGER.info("init - Starting Tag publisher.");
    LOGGER.trace("init - Registering for Tag Updates.");
//    this.alarmAggregator.registerForTagUpdates(this);
    cacheRegistrationService.registerSynchronousToAllTags(this);

    LOGGER.trace("init - Registering for Configuration Updates.");

    this.configurationUpdate.registerForConfigurationUpdates(this);

    if (republicationDelay != 0) {
      republisher.setRepublicationDelay(republicationDelay);
    }
    republisher.start();
  }

  /**
   * Before shutdown, stop republisher thread.
   */
  @PreDestroy
  public void shutdown() {
    LOGGER.info("Stopping tag publisher");
    republisher.stop();
  }

//  /**
//   * Generates for every notification a <code>TransferTagValue</code>
//   * object which is then sent as serialized GSON message trough the
//   * dedicated JMS client tag topic.
//   * @param tag the updated Tag
//   * @param alarms the new values of the associated alarms; this list
//   *               is null if no alarms are associated to the tag
//   */
//  @Deprecated
//  public void notifyOnUpdate(final Tag tag, final List<Alarm> alarms) {
//    TagWithAlarms tagWithAlarms = new TagWithAlarmsImpl(tag, alarms);
//    try {
//      publish(tagWithAlarms);
//    } catch (JmsException e) {
//      LOGGER.error("notifyOnUpdate - Error publishing tag update to topic for tag " + tagWithAlarms.getTag().getId() + " - submitting for republication", e);
//      republisher.publicationFailed(tagWithAlarms);
//    }
//  }

  @Override
  public void publish(final Tag tag) {
    TransferTagValueImpl tagValue = TransferObjectFactory.createTransferTagValue(tag);
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Publishing tag update to client: " + TransferTagSerializer.toJson(tagValue));
    }
    jmsSender.sendToTopic(TransferTagSerializer.toJson(tagValue), TopicProvider.topicFor(tag, properties));
  }


  @Override
  public void notifyOnConfigurationUpdate(Long tagId) {
    tagLocationService.acquireReadLockOnKey(tagId);
    try {
      Tag tag = this.tagFacadeGateway.getTagWithAlarms(tagId).getTag();
      try {
        String topic = TopicProvider.topicFor(tag, properties);
        TransferTagImpl transferTag = TransferObjectFactory.createTransferTag(tag, aliveTimerFacade.isRegisteredAliveTimer(tagId), topic);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("Publishing configuration update to client: " + TransferTagSerializer.toJson(transferTag));
        }
        jmsSender.sendToTopic(TransferTagSerializer.toJson(transferTag), topic);
      } catch (JmsException e) {
        LOGGER.error("Error publishing configuration update to topic for tag " + tagId
            + " - submitting for republication", e);
        republisher.publicationFailed(tag);
      }
    } finally {
      tagLocationService.releaseReadLockOnKey(tagId);
    }
  }

  /**
   * @return the total number of failed publications since the publisher start
   */
  @ManagedOperation(description = "Returns the total number of failed publication attempts since the application started")
  public long getNumberFailedPublications() {
    return republisher.getNumberFailedPublications();
  }

  /**
   * @return the number of current tag updates awaiting publication to the clients
   */
  @ManagedOperation(description = "Returns the current number of events awaiting re-publication (should be 0 in normal operation)")
  public int getSizeUnpublishedList() {
    return republisher.getSizeUnpublishedList();
  }

  @Override
  // TODO change the TagWithAlarmsImpl in order to send the expressions information to the client
  public void notifyElementUpdated(Tag tag) {
    try {
      publish(tag);
    } catch (JmsException e) {
      LOGGER.error("Error publishing tag update to topic for tag " + tag.getId() + " - submitting for republication", e);
      republisher.publicationFailed(tag);
    }

  }

  @Override
  public void confirmStatus(Tag cacheable) {
    // TODO implement this
  }
}
