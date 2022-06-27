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

import java.util.concurrent.ExecutorService;

import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.jms.EnqueuingEventListener;
import cern.c2mon.client.core.jms.SupervisionListener;
import cern.c2mon.shared.client.supervision.SupervisionEvent;

public class SupervisionTopicWrapper extends AbstractTopicWrapper<SupervisionListener, SupervisionEvent> {


  public SupervisionTopicWrapper(final SlowConsumerListener slowConsumerListener,
                                 final EnqueuingEventListener enqueuingEventListener,
                                 final ExecutorService topicPollingExecutor,
                                 final C2monClientProperties properties) {
    super(slowConsumerListener, enqueuingEventListener, topicPollingExecutor, properties.getJms().getSupervisionTopic(), properties);
  }

  @Override
  protected AbstractListenerWrapper<SupervisionListener, SupervisionEvent> createListenerWrapper(C2monClientProperties properties,
                                                                                                 SlowConsumerListener slowConsumerListener,
                                                                                                 EnqueuingEventListener enqueuingEventListener,
                                                                                                 final ExecutorService topicPollingExecutor) {
    return new SupervisionListenerWrapper(properties.getHighListenerQueueSize(), slowConsumerListener, enqueuingEventListener, topicPollingExecutor);
  }
}
