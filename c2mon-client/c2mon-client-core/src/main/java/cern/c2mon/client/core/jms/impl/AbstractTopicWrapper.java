/******************************************************************************
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
 *****************************************************************************/
package cern.c2mon.client.core.jms.impl;

import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.jms.EnqueuingEventListener;
import lombok.Getter;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.util.concurrent.ExecutorService;

import org.apache.activemq.command.ActiveMQTopic;

abstract class AbstractTopicWrapper<T, U> {

  /**
   * Topic on which server heartbeat messages are arriving.
   */
  @Getter
  private final Destination topic;

  @Getter
  private final AbstractListenerWrapper<T, U> listenerWrapper;

  public AbstractTopicWrapper(final SlowConsumerListener slowConsumerListener, final EnqueuingEventListener enqueuingEventListener, final ExecutorService topicPollingExecutor, String topicName, C2monClientProperties properties) {
    this.topic = new ActiveMQTopic(topicName);
    listenerWrapper = createListenerWrapper(properties, slowConsumerListener, enqueuingEventListener, topicPollingExecutor);
    listenerWrapper.start();
  }

  abstract AbstractListenerWrapper<T, U> createListenerWrapper(C2monClientProperties properties, SlowConsumerListener slowConsumerListener,
                                                               EnqueuingEventListener enqueuingEventListener, final ExecutorService topicPollingExecutor);

  /**
   * Subscribes to the topic. Called when refreshing all subscriptions.
   *
   * @param connection
   *          The JMS connection
   * @throws JMSException
   *           if problem subscribing
   */
  public void subscribeToTopic(Connection connection) throws JMSException {
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer consumer = session.createConsumer(topic);
    consumer.setMessageListener(listenerWrapper);
  }

  public void removeListener(T listener) {
    if (listener != null) {
      listenerWrapper.removeListener(listener);
    }
  }

  public void addListener(T listener) {
    if (listener != null) {
      listenerWrapper.addListener(listener);
    }
  }

  public int getQueueSize() {
    return listenerWrapper.getQueueSize();
  }

  public void stop() {
    listenerWrapper.stop();
  }
}
