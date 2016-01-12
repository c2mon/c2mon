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
package cern.c2mon.client.ext.messenger.impl;

import cern.c2mon.client.common.admin.BroadcastMessage;
import cern.c2mon.client.common.admin.BroadcastMessageDeliveryException;
import cern.c2mon.client.common.admin.BroadcastMessageImpl;
import cern.c2mon.client.ext.messenger.BroadcastMessageHandler;
import cern.c2mon.client.jms.BroadcastMessageListener;
import cern.c2mon.client.jms.JmsProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Destination;
import javax.jms.JMSException;

/**
 * Implements the {@link BroadcastMessageHandler} and has the methods necessary to
 * listen to {@link BroadcastMessage}, and to publish them.
 * 
 * @author vdeila
 */
@Service
public class BroadcastMessageHandlerImpl implements BroadcastMessageHandler {

  /** The jms proxy */
  private final JmsProxy jmsProxy;

  /** The name of the message topic where broadcast messages are sent */
  private final String messageTopicName;

  /** The time to live after a message is sent */
  private final long messageTimeout;

  @Autowired
  public BroadcastMessageHandlerImpl(
      final JmsProxy jmsProxy, 
      @Qualifier("broadcastMessageTopic") final Destination broadcastMessageTopic,
      @Qualifier("broadcastMessageTopicName") final String broadcastMessageTopicName,
      @Qualifier("broadcastMessageTimeout") final Long broadcastMessageTimeout) {
    
    this.jmsProxy = jmsProxy;
    this.messageTopicName = broadcastMessageTopicName;
    this.messageTimeout = broadcastMessageTimeout;
    this.jmsProxy.setBroadcastMessageTopic(broadcastMessageTopic);
  }

  @Override
  public void registerMessageListener(final BroadcastMessageListener broadcastMessageListener) {
    this.jmsProxy.registerBroadcastMessageListener(broadcastMessageListener);
  }

  @Override
  public void unregisterMessageListener(final BroadcastMessageListener broadcastMessageListener) {
    this.jmsProxy.unregisterBroadcastMessageListener(broadcastMessageListener);
  }

  @Override
  public void publishMessage(final BroadcastMessage broadcastMessage) throws BroadcastMessageDeliveryException {
    final BroadcastMessageImpl message;
    if (broadcastMessage instanceof BroadcastMessageImpl) {
      message = (BroadcastMessageImpl) broadcastMessage;
    }
    else {
      message = new BroadcastMessageImpl(broadcastMessage);
    }
    try {
      jmsProxy.publish(message.toJson(), messageTopicName, messageTimeout);
    }
    catch (JMSException e) {
      throw new BroadcastMessageDeliveryException("Failed to deliver the broadcast message", e);
    }
  }

}
