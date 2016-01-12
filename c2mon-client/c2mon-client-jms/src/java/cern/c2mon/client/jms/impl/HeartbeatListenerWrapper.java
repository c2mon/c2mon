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
package cern.c2mon.client.jms.impl;

import java.util.concurrent.ExecutorService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import cern.c2mon.client.jms.HeartbeatListener;
import cern.c2mon.shared.client.supervision.Heartbeat;
import cern.c2mon.shared.util.json.GsonFactory;

import com.google.gson.Gson;

/**
 * Implementation of an AbstractListenerWrapper for subscribing
 * to the heartbeat topic and distributing events to listeners.
 * 
 * @author Mark Brightwell
 *
 */
public class HeartbeatListenerWrapper extends AbstractListenerWrapper<HeartbeatListener, Heartbeat> {
  
  /**
   * Get C2MON Gson instance for decoding Json message.
   */
  private Gson gson = GsonFactory.createGson();

  /**
   * Constructor.
   * @param queueCapacity size of event queue
   * @param slowConsumerListener listener registered for JMS problem callbacks
   * @param executorService thread pool managing updates
   */
  public HeartbeatListenerWrapper(int queueCapacity, SlowConsumerListener slowConsumerListener, final ExecutorService executorService) {
    super(queueCapacity, slowConsumerListener, executorService);  
  }
  
  @Override
  protected Heartbeat convertMessage(final Message message) throws JMSException {
    return gson.fromJson(((TextMessage) message).getText(), Heartbeat.class);
  }

  @Override
  protected void invokeListener(final HeartbeatListener listener, final Heartbeat event) {
    listener.onHeartbeat(event);
  }

  @Override
  protected String getDescription(Heartbeat event) {
    return "Heartbeat message with timestamp " + event.getTimestamp();
  }

  @Override
  protected boolean filterout(Heartbeat event) {    
    return false;
  }

}
