/******************************************************************************
 * Copyright (C) 2010-2018 CERN. All rights not expressly granted are reserved.
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import cern.c2mon.client.core.jms.EnqueuingEventListener;
import lombok.extern.slf4j.Slf4j;

import cern.c2mon.client.core.listener.TagUpdateListener;
import cern.c2mon.shared.client.serializer.TransferTagSerializer;
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
@Slf4j
class MessageListenerWrapper extends AbstractQueuedWrapper<TagValueUpdate> {

    /**
     * Wrapped listener. Methods accessing this field are synchronized.
     */
    private Map<Long, TagUpdateListener> listeners = new HashMap<>();

    /**
     * Timestamps of tag updates used to filter out older events.
     */
    private ConcurrentHashMap<Long, Long> eventTimes = new ConcurrentHashMap<>();

    /**
     * Constructor. Adds the listener to receive updates for the specified Tag id.
     *
     * @param tagId the ClientDataTag id
     * @param serverUpdateListener the listener that should be registered
     * @param executorService thread pool polling the queue
     */
    public MessageListenerWrapper(final Long tagId, final TagUpdateListener serverUpdateListener,
                                  final int queueCapacity, final SlowConsumerListener slowConsumerListener,
                                  final EnqueuingEventListener enqueuingEventListener,
                                  final ExecutorService executorService) {
        super(queueCapacity, slowConsumerListener, enqueuingEventListener, executorService);
        addListener(serverUpdateListener, tagId);
        log.info("MessageListenerWrapper queue size : " + queueCapacity);
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
        return TransferTagSerializer.fromJson(((TextMessage) message).getText(), TransferTagValueImpl.class);
    }

    @Override
    protected synchronized void notifyListeners(TagValueUpdate tagValueUpdate) {
        if (listeners.containsKey(tagValueUpdate.getId())) {
            if (!filterout(tagValueUpdate)) {
                log.trace("notifying listener about TagValueUpdate event. tag id: {}, value: {}, timestamp: {}",
                        tagValueUpdate.getId(), tagValueUpdate.getValue(), tagValueUpdate.getServerTimestamp());
                listeners.get(tagValueUpdate.getId()).onUpdate(tagValueUpdate);
            }
        } else {
            log.trace("no subscribed listener for TagValueUpdate event. tag id: {}, value: {}, timestamp: {} - filtering out",
                    tagValueUpdate.getId(), tagValueUpdate.getValue(), tagValueUpdate.getServerTimestamp());
        }
    }

    private boolean filterout(TagValueUpdate tagValueUpdate) {
        Long oldTime = eventTimes.get(tagValueUpdate.getId());
        Long newTime = tagValueUpdate.getServerTimestamp().getTime();
        if (oldTime == null || oldTime <= newTime) {
            eventTimes.put(tagValueUpdate.getId(), newTime);
            return false;
        } else {
            log.warn("Filtering out tag update as newer update already received (tag id: {}, value: {})", tagValueUpdate.getId(), tagValueUpdate.getValue());
            return true;
        }
    }

    @Override
    protected String getDescription(TagValueUpdate event) {
        return "Update for tag " + event.getId() + " (value: " + event.getValue() + ")";
    }

    @Override
    protected String getQueueName() {
        return "Tag";
    }

}
