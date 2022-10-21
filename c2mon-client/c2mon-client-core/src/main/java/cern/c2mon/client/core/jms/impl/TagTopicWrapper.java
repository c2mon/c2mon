/*******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
 ******************************************************************************/
package cern.c2mon.client.core.jms.impl;

import java.util.concurrent.ExecutorService;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQTopic;

import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.jms.EnqueuingEventListener;
import cern.c2mon.shared.client.tag.TagUpdate;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of an {@link AbstractTopicWrapper} for receiving live tag updates,
 * nested in a normal {@link TagUpdate} object.
 */
@Slf4j
public class TagTopicWrapper extends AbstractTopicWrapper<TagListener, TagUpdate> {

  private Session tagSession;

  private MessageConsumer tagConsumer;

  private final Destination[] topics;

  public TagTopicWrapper(final SlowConsumerListener slowConsumerListener,
                           final EnqueuingEventListener enqueuingEventListener,
                           final ExecutorService topicPollingExecutor,
                           final C2monClientProperties properties) {
    // TODO this gives compilation error, don't know how to up
    //super(slowConsumerListener, enqueuingEventListener, topicPollingExecutor, properties.getJms().getDataTagTopic(), properties);
    //this.topics = new Destination[]{new ActiveMQTopic(properties.getJms().getDataTagTopic()),
    //        new ActiveMQTopic(properties.getJms().getControlTagTopic())};
    super(slowConsumerListener, enqueuingEventListener, topicPollingExecutor,
            properties.getJms().getAlarmTopic().substring(0, properties.getJms().getAlarmTopic().indexOf(".")) + ".client.tag.*",
            properties);
    String domain = properties.getJms().getAlarmTopic().substring(0, properties.getJms().getAlarmTopic().indexOf("."));

    this.topics = new Destination[]{new ActiveMQTopic(domain + ".client.tag.*"),
            new ActiveMQTopic(properties.getJms().getControlTagTopic())};
  }

  @Override
  public void subscribeToTopic(Connection connection) throws JMSException {
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    for (Destination topic : this.topics) {
      log.debug("Subscribing TagTopicWrapper to topic {}", topic);
      MessageConsumer consumer = session.createConsumer(topic);
      consumer.setMessageListener(this.getListenerWrapper());
    }
  }

  @Override
  protected AbstractListenerWrapper<TagListener, TagUpdate> createListenerWrapper(C2monClientProperties properties,
                                                                                    SlowConsumerListener slowConsumerListener,
                                                                                    EnqueuingEventListener enqueuingEventListener,
                                                                                    final ExecutorService topicPollingExecutor) {
    return new TagListenerWrapper(properties.getHighListenerQueueSize(), slowConsumerListener, enqueuingEventListener, topicPollingExecutor);
  }

  /**
   * Unsubscribes from the tag topic.
   * @throws JMSException if problem subscribing
   */
  protected void unsubscribeFromTagTopic() throws JMSException {
    tagSession.close();
    tagSession = null;
    tagConsumer.close();
    tagConsumer = null;
    log.debug("Successfully unsubscribed from tag topic");
  }
}
