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

import cern.c2mon.client.core.jms.SupervisionListener;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Wrapper JMS listener to register to the supervision topic.
 * This class then notifies all registered listeners.
 * 
 * <p>Is thread-safe: methods are synchronized to prevent concurrent calls
 * to add, remove and onMessage (which use the collection).
 * 
 * @author Mark Brightwell
 *
 */
class SupervisionListenerWrapper extends AbstractListenerWrapper<SupervisionListener, SupervisionEvent>{

  /**
   * Timestamps of supervision events to filter out older events.
   */
  private ConcurrentHashMap<String, Long> eventTimes = new ConcurrentHashMap<String, Long>();
  
  /**
   * Constructor.
   * @param queueCapacity size of event queue
   * @param slowConsumerListener listener registered for JMS problem callbacks
   * @param executorService thread pool polling the queue
   */
  public SupervisionListenerWrapper(int queueCapacity, SlowConsumerListener slowConsumerListener, final ExecutorService executorService) {
    super(queueCapacity, slowConsumerListener, executorService);  
  }

  @Override
  protected SupervisionEvent convertMessage(Message message) throws JMSException {
    return SupervisionEventImpl.fromJson(((TextMessage) message).getText());
  }

  @Override
  protected void invokeListener(SupervisionListener listener, SupervisionEvent event) {    
    listener.onSupervisionUpdate(event);
  }

  @Override
  protected String getDescription(SupervisionEvent event) {
    return "Supervision message for " + event.getEntity() + " " + event.getEntityId();
  }

  @Override
  protected boolean filterout(SupervisionEvent event) {
    String eventKey = event.getEntity().toString() + event.getEntityId()+"";
    Long oldTime = eventTimes.get(eventKey);
    Long newTime = event.getEventTime().getTime();
    if (oldTime == null || oldTime <= newTime) {
      eventTimes.put(eventKey, newTime);
      return false;
    } else {
      return true;
    }   
  }

}
