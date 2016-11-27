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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.shared.client.serializer.TransferTagSerializer;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.rule.RuleFormatException;
import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper JMS listener to register to the alarm messages topic. This
 * class then notifies all registered listeners.
 */
@Slf4j
class AlarmListenerWrapperNew extends AbstractListenerWrapper<BaseTagListener, TagUpdate> {

  /**
   * Timestamps of events to filter out older events.
   */
  private ConcurrentHashMap<Long, Long> eventTimes = new ConcurrentHashMap<Long, Long>();

  public AlarmListenerWrapperNew(int queueCapacity, SlowConsumerListener slowConsumerListener, final ExecutorService executorService) {
    super(queueCapacity, slowConsumerListener, executorService);
  }

  @Override
  protected TagUpdate convertMessage(final Message message) throws JMSException {

    return TransferTagSerializer.fromJson(((TextMessage) message).getText(), TransferTagImpl.class);
  }

  @Override
  protected void invokeListener(final BaseTagListener listener, final TagUpdate tag) {
    try {
      log.debug("InvokeListener: {} for tag with alarm expression id:{}", listener.getClass(), tag.getId());
      ClientDataTagImpl tagWithAlarm = new ClientDataTagImpl(tag.getId());
      tagWithAlarm.update(tag);
      listener.onUpdate(tagWithAlarm);

    } catch (RuleFormatException e) {
      log.error("Received a tag on the alarm topic which triggered a RuleFormatException", e);
    }
  }

  @Override
  protected String getDescription(TagUpdate event) {
    return "Tag value update for alarm " + event.getId();
  }

  @Override
  protected boolean filterout(TagUpdate event) {
    Long oldTime = eventTimes.get(event.getId());
    Long newTime = event.getServerTimestamp().getTime();
    if (oldTime == null || oldTime <= newTime) {
      eventTimes.put(event.getId(), newTime);
      return false;
    } else {
      return true;
    }
  }
}
