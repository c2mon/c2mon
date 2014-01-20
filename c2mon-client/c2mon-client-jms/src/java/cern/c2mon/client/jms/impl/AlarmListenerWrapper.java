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
package cern.c2mon.client.jms.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.shared.util.json.GsonFactory;

import com.google.gson.Gson;

/**
 * Wrapper JMS listener to register to the alarm messages topic. This
 * class then notifies all registered listeners.<br/>
 * <br/>
 */
class AlarmListenerWrapper extends AbstractListenerWrapper<AlarmListener, AlarmValue> {
  
  /** Class logger. */
  private static final Logger LOGGER = Logger.getLogger(AlarmListenerWrapper.class);
  
  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();
  
  /**
   * Timestamps of events to filter out older events.
   */
  private ConcurrentHashMap<Long, Long> eventTimes = new ConcurrentHashMap<Long, Long>();
  
  /**
   * Constructor.
   * @param queueCapacity size of event queue
   * @param slowConsumerListener listener registered for JMS problem callbacks
   * @param executorService threads pooling queue
   */
  public AlarmListenerWrapper(int queueCapacity, SlowConsumerListener slowConsumerListener, final ExecutorService executorService) {
    super(queueCapacity, slowConsumerListener, executorService);     
  }
   
  @Override
  protected AlarmValue convertMessage(final Message message) throws JMSException {
    
    return GSON.fromJson(((TextMessage) message).getText(), AlarmValueImpl.class);
  }

  @Override
  protected void invokeListener(final AlarmListener listener, final AlarmValue alarm) {
    
    LOGGER.debug("AlarmListenerWrapper invokeListener: " + listener.getClass()
        + " for alarm id:" + alarm.getId());
    listener.onAlarmUpdate(alarm);
  }

  @Override
  protected String getDescription(AlarmValue event) {
    return "AlarmValue for alarm " + event.getId();
  }

  @Override
  protected boolean filterout(AlarmValue event) {        
    Long oldTime = eventTimes.get(event.getId());
    Long newTime = event.getTimestamp().getTime();
    if (oldTime == null || oldTime <= newTime) {
      eventTimes.put(event.getId(), newTime);
      return false;
    } else {
      return true;
    }
  }
}
