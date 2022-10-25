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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import com.google.gson.Gson;

import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.core.jms.EnqueuingEventListener;
import cern.c2mon.client.core.tag.TagController;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.rule.RuleFormatException;
import cern.c2mon.shared.util.json.GsonFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper JMS listener to register to the tag messages topic. This
 * class then notifies all registered listeners.<br/>
 * <br/>
 */
@Slf4j
class TagListenerWrapper extends AbstractListenerWrapper<TagListener, TagUpdate> {

  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();

  /**
   * Timestamps of events to filter out older events.
   */
  private ConcurrentHashMap<Long, Long> eventTimes = new ConcurrentHashMap<>();

  /**
   * Constructor.
   * @param queueCapacity size of event queue
   * @param slowConsumerListener listener registered for JMS problem callbacks
   * @param executorService threads pooling queue
   */
  public TagListenerWrapper(int queueCapacity, SlowConsumerListener slowConsumerListener,
                              EnqueuingEventListener enqueuingEventListener, final ExecutorService executorService) {
    super(queueCapacity, slowConsumerListener, enqueuingEventListener, executorService);
    log.info("TagListenerWrapper queue size : " + queueCapacity);
  }

  @Override
  protected TagUpdate convertMessage(final Message message) throws JMSException {
    return GSON.fromJson(((TextMessage) message).getText(), TransferTagImpl.class);
  }


  @Override
  protected void invokeListener(final TagListener listener, final TagUpdate tagUpdate) {
    log.debug("Invoke listener class {} for tag id: {}", listener.getClass(), tagUpdate.getId());

    TagController controller = new TagController(tagUpdate.getId());
    try {
      controller.update(tagUpdate);
      listener.onUpdate(controller.getTagImpl());
    } catch (RuleFormatException e) {
      log.error("Rule format error. Cannot inform listeners about tag update on tag #{}", tagUpdate.getId(),
              e);
    } catch (Exception ex) {
      log.error("Error caught on tag notification!", ex);
    }
  }

  @Override
  protected String getDescription(TagUpdate event) {
    return "Tag #" + event.getId() + " got tag value change";
  }

  @Override
  protected String getQueueName() {
    return "Tag";
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