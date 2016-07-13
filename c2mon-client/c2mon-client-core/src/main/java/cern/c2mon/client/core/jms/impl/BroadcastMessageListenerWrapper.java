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
package cern.c2mon.client.core.jms.impl;

import java.util.concurrent.ExecutorService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import cern.c2mon.client.common.admin.BroadcastMessage;
import cern.c2mon.client.common.admin.BroadcastMessageImpl;
import cern.c2mon.client.core.jms.BroadcastMessageListener;

/**
 * Wrapper JMS listener to register to the administrator messages topic. This
 * class then notifies all registered listeners.<br/>
 * <br/>
 * Is thread-safe: methods are synchronized to prevent concurrent calls to add,
 * remove and onMessage (which use the collection).
 * 
 * @author vdeila
 * 
 */
class BroadcastMessageListenerWrapper extends AbstractListenerWrapper<BroadcastMessageListener, BroadcastMessage> {

  /**
   * Constructor.
   * @param queueCapacity size of event queue
   * @param slowConsumerListener listener registered for JMS problem callbacks
   */
  public BroadcastMessageListenerWrapper(int queueCapacity, SlowConsumerListener slowConsumerListener, final ExecutorService executorService) {
    super(queueCapacity, slowConsumerListener, executorService);    
  }

  @Override
  protected BroadcastMessage convertMessage(Message message) throws JMSException {
    return BroadcastMessageImpl.fromJson(((TextMessage) message).getText());
  }

  @Override
  protected void invokeListener(final BroadcastMessageListener listener, final BroadcastMessage event) {
    listener.onBroadcastMessageReceived(event);
  }

  @Override
  protected String getDescription(BroadcastMessage event) {
    return "Admin message: " + event.getMessage();
  }

  @Override
  protected boolean filterout(BroadcastMessage event) {
    return false;
  }

}
