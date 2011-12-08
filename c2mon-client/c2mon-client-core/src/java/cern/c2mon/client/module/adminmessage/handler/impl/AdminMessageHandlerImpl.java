/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.module.adminmessage.handler.impl;

import javax.jms.Destination;
import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.admin.AdminMessage;
import cern.c2mon.client.common.admin.AdminMessageDeliveryException;
import cern.c2mon.client.common.admin.AdminMessageImpl;
import cern.c2mon.client.jms.AdminMessageListener;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.module.adminmessage.handler.AdminMessageHandler;

/**
 * Implements the {@link AdminMessageHandler} and have the methods necessary to
 * listen to {@link AdminMessage}, and to publish them.
 * 
 * @author vdeila
 */
@Service
public class AdminMessageHandlerImpl implements AdminMessageHandler {

  /** The jms proxy */
  private final JmsProxy jmsProxy;

  /** The name of the admin message topic where admin messages is sent */
  private final String adminMessageTopicName;

  /** The time to live after an admin message is sent */
  private final long adminMessageTimeout;

  @Autowired
  public AdminMessageHandlerImpl(
      final JmsProxy jmsProxy, 
      @Qualifier("adminMessageTopic") final Destination adminMessageTopic,
      @Qualifier("adminMessageTopicName") final String adminMessageTopicName, 
      @Qualifier("adminMessageTimeout") final Long adminMessageTimeout) {
    
    this.jmsProxy = jmsProxy;
    this.adminMessageTopicName = adminMessageTopicName;
    this.adminMessageTimeout = adminMessageTimeout;
    this.jmsProxy.setAdminMessageTopic(adminMessageTopic);
  }

  @Override
  public void registerAdminMessageListener(final AdminMessageListener adminMessageListener) {
    this.jmsProxy.registerAdminMessageListener(adminMessageListener);
  }

  @Override
  public void unregisterAdminMessageListener(final AdminMessageListener adminMessageListener) {
    this.jmsProxy.unregisterAdminMessageListener(adminMessageListener);
  }

  @Override
  public void publishAdminMessage(final AdminMessage adminMessage) throws AdminMessageDeliveryException {
    final AdminMessageImpl message;
    if (adminMessage instanceof AdminMessageImpl) {
      message = (AdminMessageImpl) adminMessage;
    }
    else {
      message = new AdminMessageImpl(adminMessage);
    }
    try {
      jmsProxy.publish(message.toJson(), adminMessageTopicName, adminMessageTimeout);
    }
    catch (JMSException e) {
      throw new AdminMessageDeliveryException("Failed to deliver the admin message", e);
    }
  }

}
