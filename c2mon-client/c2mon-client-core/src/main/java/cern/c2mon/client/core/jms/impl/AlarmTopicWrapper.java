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

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import cern.c2mon.client.core.jms.EnqueuingEventListener;
import lombok.extern.slf4j.Slf4j;

import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.jms.AlarmListener;
import cern.c2mon.shared.client.tag.TagUpdate;

/**
 * Implementation of an {@link AbstractTopicWrapper} for receiving live alarms updates,
 * nested in a normal {@link TagUpdate} object.
 *
 * @author Matthias Braeger
 */
@Slf4j
public class AlarmTopicWrapper extends AbstractTopicWrapper<AlarmListener, TagUpdate> {

  private Session alarmSession;

  private MessageConsumer alarmConsumer;


  public AlarmTopicWrapper(final SlowConsumerListener slowConsumerListener,
                           final EnqueuingEventListener enqueuingEventListener,
                           final ExecutorService topicPollingExecutor,
                           final C2monClientProperties properties) {
    super(slowConsumerListener, enqueuingEventListener, topicPollingExecutor, properties.getJms().getAlarmWithTagTopic(), properties);
  }

  @Override
  protected AbstractListenerWrapper<AlarmListener, TagUpdate> createListenerWrapper(C2monClientProperties properties,
                                                                                    SlowConsumerListener slowConsumerListener,
                                                                                    EnqueuingEventListener enqueuingEventListener,
                                                                                    final ExecutorService topicPollingExecutor) {
    return new AlarmListenerWrapper(properties.getHighListenerQueueSize(), slowConsumerListener, enqueuingEventListener, topicPollingExecutor);
  }

  /**
   * Unsubscribes from the alarm topic.
   * @throws JMSException if problem subscribing
   */
  protected void unsubscribeFromAlarmTopic() throws JMSException {
    alarmSession.close();
    alarmSession = null;
    alarmConsumer.close();
    alarmConsumer = null;
    log.debug("Successfully unsubscribed from alarm topic");
  }
}
