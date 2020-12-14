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
package cern.c2mon.server.client.publish;

import cern.c2mon.cache.actions.alarm.AlarmAggregator;
import cern.c2mon.cache.actions.alarm.AlarmAggregatorListener;
import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.server.client.config.ClientProperties;
import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.serializer.TransferTagSerializer;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.daq.republisher.Publisher;
import cern.c2mon.shared.daq.republisher.Republisher;
import cern.c2mon.shared.daq.republisher.RepublisherFactory;
import cern.c2mon.shared.util.jms.JmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.jms.JmsException;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Publishes active alarms and the corresponding tag value to the C2MON client applications on the
 * alarm publication topic.
 *
 * <p>Will attempt re-publication of alarms if JMS connection fails.
 *
 * @author Matthias Braeger
 */
@Slf4j
@Service
@ManagedResource(description = "Bean publishing Alarm updates (TagWithAlarms) to the clients")
public class AlarmPublisher implements SmartLifecycle, AlarmAggregatorListener, Publisher<TagWithAlarms> {

    /**
     * Bean providing for sending JMS messages and waiting for a response
     */
    private final JmsSender jmsSender;

    private final C2monCache<Alarm> alarmCache;

    /**
     * Reference to the tag location service to check whether a tag exists
     */
    private final TagCacheCollection unifiedTagCacheFacade;

    /**
     * The configured JMS alarm topic, extracted from {@link ClientProperties}
     */
    private final String alarmWithTagTopic;

    /**
     * Listens for Tag updates, evaluates all associated alarms and passes the result
     */
    private final AlarmAggregator alarmAggregator;

    /**
     * Contains re-publication logic
     */
    private final Republisher<TagWithAlarms> republisher;

    private final AliveTagService aliveTagService;

    /**
     * Lifecycle flag
     */
    private boolean running;

    /**
     * Default Constructor
     *
     * @param jmsSender             Used for sending JMS messages and waiting for a response.
     * @param alarmCache            Used to register to Alarm updates.
     * @param unifiedTagCacheFacade Reference to the tag location service singleton.
     * @param alarmAggregator       Required to receive notifications when a tag has changed
     * @param properties            The configured {@link ClientProperties}. Required to determine the JMS alarm topic.
     */
    @Autowired
    public AlarmPublisher(@Qualifier("alarmTopicPublisher") final JmsSender jmsSender,
                          final C2monCache<Alarm> alarmCache,
                          final TagCacheCollection unifiedTagCacheFacade,
                          final AlarmAggregator alarmAggregator,
                          final AliveTagService aliveTagService,
                          final ClientProperties properties) {

        this.jmsSender = jmsSender;
        this.alarmCache = alarmCache;
        this.alarmAggregator = alarmAggregator;
        this.unifiedTagCacheFacade = unifiedTagCacheFacade;
        this.aliveTagService = aliveTagService;
        this.alarmWithTagTopic = properties.getJms().getAlarmWithTagTopic();

        republisher = RepublisherFactory.createRepublisher(this, "TagWithAlarms");
    }

    /**
     * Registering this listener to alarms.
     */
    @PostConstruct
    void init() {
        //TODO next line required?
        //alarmCache.getCacheListenerManager().registerListener(this::notifyElementUpdated
        //    , CacheEvent.UPDATE_ACCEPTED, CacheEvent.CONFIRM_STATUS);
        this.alarmAggregator.registerForTagUpdates(this);
    }



    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void start() {
        log.debug("Starting Alarm publisher for TagWithAlarms");
        running = true;
        republisher.start();
    }

    @Override
    public void stop() {
        log.debug("Stopping Alarm publisher for TagWithAlarms");
        republisher.stop();
        running = false;
    }

    /**
     * @return the number of current tag updates awaiting publication to the clients
     */
    @ManagedOperation(description = "Returns the current number of alarms awaiting re-publication (should be 0 in normal operation)")
    public int getSizeUnpublishedList() {
        return republisher.getSizeUnpublishedList();
    }

    /**
     * Send update for all alarms that changed
     */
    @Override
    public void notifyOnUpdate(Tag tag, List<Alarm> alarms) {
        if (alarms != null) {
            List<Alarm> changedAlarms = alarms.stream()
                    .filter(a -> a.getSourceTimestamp().equals(tag.getTimestamp())).collect(Collectors.toList());

            if (!changedAlarms.isEmpty()) {
                publish(new TagWithAlarms<>(tag, changedAlarms));
            }
        }
    }

    @Override
    public void publish(final TagWithAlarms tagWithAlarms) {
        boolean isRegisteredAliveTimer = aliveTagService.isRegisteredAliveTimer(tagWithAlarms.getTag().getId());
        TransferTagValueImpl tagValue = TransferObjectFactory.createTransferTag(tagWithAlarms, isRegisteredAliveTimer, "N/A");

        if (log.isTraceEnabled()) {
            log.trace("Publishing alarm(s) with full tag object for tag id #{} to topic {}", tagWithAlarms.getTag().getId(), alarmWithTagTopic);
        }

        try {
            jmsSender.sendToTopic(TransferTagSerializer.toJson(tagValue), alarmWithTagTopic);
        } catch (JmsException e) {
            log.error("Error publishing alarm(s) with full tag object to clients - submitting for republication for tag #{} + alarms", tagWithAlarms.getTag().getId(), e);
            republisher.publicationFailed(tagWithAlarms);
        }

    }
}
