/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2005-2011 CERN. This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.jms.impl;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;

/**
 * Class used locally to wrap Tag update listeners in a JMS message listener. The JmsProxy subscribes a
 * MessageListenerWrapper to receive TransferTagValue objects on a given topic.
 * <p>
 * Tag update listeners can register with this wrapper to receive updates for a specific Tag (specified by id). The
 * wrapper listens on a the given topic and notifies {@link TagUpdateListener}s when an update is received for the
 * corresponding Tag. Notice only one TagUpdateListener will be registered for a given id (the latest one added). In
 * other words, this wrapper also functions as a filter on the topic, with undesired messages being filtered out.
 * 
 * @author Mark Brightwell
 */
class MessageListenerWrapper extends AbstractQueuedWrapper<TagValueUpdate> {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = Logger.getLogger(MessageListenerWrapper.class);

    /**
     * Wrapped listener. Methods accessing this field are synchronized.
     */
    private Map<Long, TagUpdateListener> listeners = new HashMap<Long, TagUpdateListener>();
    
    /**
     * Timestamps of tag updates used to filter out older events.
     */
    private ConcurrentHashMap<Long, Long> eventTimes = new ConcurrentHashMap<Long, Long>();

    /**
     * Constructor. Adds the listener to receive updates for the specified Tag id.
     * 
     * @param tagId the ClientDataTag id
     * @param serverUpdateListener the listener that should be registered
     * @param executorService thread pool polling the queue
     */
    public MessageListenerWrapper(final Long tagId, final TagUpdateListener serverUpdateListener,
            final int queueCapacity, final SlowConsumerListener slowConsumerListener,
            final ExecutorService executorService) {
        super(queueCapacity, slowConsumerListener, executorService);
        addListener(serverUpdateListener, tagId);
    }

    /**
     * Registers the listener for update notifications for the specified Tag. Assumes this object is registered as JMS
     * listener on the correct topic for the given Tag.
     * 
     * @param listener the listener to notify on update
     * @param tagId listens to updates for the ClientDataTag with this id
     */
    public synchronized void addListener(final TagUpdateListener listener, final Long tagId) {
        listeners.put(tagId, listener);
    }

    /**
     * Removes any listener registered for update notifications for this Tag.
     * 
     * @param tagId the id of the Tag
     */
    public synchronized void removeListener(final Long tagId) {
        listeners.remove(tagId);
        eventTimes.remove(tagId);
    }

    /**
     * Returns true if their are currently no listeners registered for this topic (the wrapper class can then
     * unsubscribe).
     * 
     * @return true if no listeners
     */
    public synchronized boolean isEmpty() {
        return listeners.isEmpty();
    }

    @Override
    protected TagValueUpdate convertMessage(Message message) throws JMSException {
        return TransferTagValueImpl.fromJson(((TextMessage) message).getText());
    }

    @Override
    protected synchronized void notifyListeners(TagValueUpdate tagValueUpdate) {

        if (listeners.containsKey(tagValueUpdate.getId())) {
            if (!filterout(tagValueUpdate)) { 
              if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(format(
                        "notifying listener about TagValueUpdate event. tag id: %d  value: %s timestamp: %s",
                        tagValueUpdate.getId(), tagValueUpdate.getValue(), tagValueUpdate.getServerTimestamp()));
              }
              listeners.get(tagValueUpdate.getId()).onUpdate(tagValueUpdate);                           
            }                       
        } else {
          if (LOGGER.isTraceEnabled()) {
              LOGGER.trace(format(
                      "no subscribed listener for TagValueUpdate event. tag id: %d  value: %s timestamp: %s - filtering out",
                      tagValueUpdate.getId(), tagValueUpdate.getValue(), tagValueUpdate.getServerTimestamp()));
          }
        }
          
    }

    private boolean filterout(TagValueUpdate tagValueUpdate) {      
      Long oldTime = eventTimes.get(tagValueUpdate.getId());
      Long newTime = tagValueUpdate.getServerTimestamp().getTime();
      if (oldTime == null || oldTime <= newTime) {
        eventTimes.put(tagValueUpdate.getId(), newTime);
        return false;
      } else {
        LOGGER.warn(format("Filtering out Tag update as newer update already received (tag id: %d, value: %s)", tagValueUpdate.getId(), tagValueUpdate.getValue()));
        return true;
      }
    }

    @Override
    protected String getDescription(TagValueUpdate event) {
        return "Update for tag " + event.getId() + " (value: " + event.getValue() + ")";
    }

}
