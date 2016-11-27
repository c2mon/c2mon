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

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import cern.c2mon.server.cache.ComparableCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.client.config.ClientProperties;
import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.server.common.republisher.Publisher;
import cern.c2mon.server.common.republisher.Republisher;
import cern.c2mon.server.common.republisher.RepublisherFactory;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.expression.Expression;
import cern.c2mon.shared.client.serializer.TransferTagSerializer;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.util.jms.JmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

/**
 * Publishes active tags with alarm expressions to the C2MON client
 * applications on the alarm publication topic.
 *
 * <p>Will attempt re-publication of tags with alarm expressions if JMS
 * connection fails.
 *
 * @author Franz Ritter
 */
@Service
@Slf4j
@ManagedResource(description = "Bean publishing tag updates with an alarm to the clients")
public class AlarmExpressionPublisher implements ComparableCacheListener<Tag>, Publisher<Tag> {

  private ClientProperties properties;

  /** Bean providing for sending JMS messages and waiting for a response */
  private final JmsSender jmsSender;

  /** Used to register to Alarm updates */
  private CacheRegistrationService cacheRegistrationService;

  /** Contains re-publication logic */
  private Republisher<Tag> republisher;

  @Autowired
  public AlarmExpressionPublisher(@Qualifier("alarmTopicPublisher") final JmsSender jmsSender,
                                  final CacheRegistrationService cacheRegistrationService,
                                  final ClientProperties properties) {
    this.jmsSender = jmsSender;
    this.cacheRegistrationService = cacheRegistrationService;
    this.properties = properties;
    this.republisher = RepublisherFactory.createRepublisher(this, "Alarm expression");
  }

  /**
   * Register this listener to alarm expressions.
   */
  @PostConstruct
  void init() {
    log.info("init - Starting alarm expression publisher");
    cacheRegistrationService.registerToAlarmExpressions(this);
    republisher.start();
  }

  /**
   * Before shutdown, stop republisher thread.
   */
  @PreDestroy
  public void shutdown() {
    log.info("Stopping Alarm publisher");
    republisher.stop();
  }

  @Override
  public void notifyElementUpdated(Tag original, Tag updated) {
    if (original.getExpressions().stream().filter(Expression::getAlarm).findFirst().isPresent()) {
      Collection<Expression> oldExpressions = original.getExpressions();
      Collection<Expression> newExpressions = updated.getExpressions();

      if (!oldExpressions.equals(newExpressions)) {
        try {
          publish(updated);
        } catch (JmsException e) {
          log.error("Error publishing tag with a changed alarm to clients - submitting for republication." +
              " Tag id is " + updated.getId(), e);
          republisher.publicationFailed(updated);
        }
      }
    }
  }

  @Override
  public void publish(final Tag tag) {
    TransferTagImpl tagValue = TransferObjectFactory.createTransferTag(tag, false, TopicProvider.topicFor(tag, properties));
    log.trace("publish - Publishing tag with alarm change to client: " + TransferTagSerializer.toJson(tagValue));
    jmsSender.send(TransferTagSerializer.toJson(tagValue));
  }

  /**
   * @return the total number of failed publications since the publisher start
   */
  @ManagedOperation(description = "Returns the total number of failed alarm publication attempts since the " +
      "application started")
  public long getNumberFailedPublications() {
    return republisher.getNumberFailedPublications();
  }

  /**
   * @return the number of current tag updates awaiting publication to the clients
   */
  @ManagedOperation(description = "Returns the current number of alarms awaiting re-publication (should be 0 in " +
      "normal operation)")
  public int getSizeUnpublishedList() {
    return republisher.getSizeUnpublishedList();
  }
}
