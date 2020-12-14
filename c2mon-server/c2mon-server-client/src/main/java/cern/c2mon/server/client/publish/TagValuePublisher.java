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
import cern.c2mon.cache.actions.alarm.AlarmService;
import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.server.client.config.ClientProperties;
import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.configuration.ConfigurationUpdate;
import cern.c2mon.server.configuration.ConfigurationUpdateListener;
import cern.c2mon.shared.client.serializer.TransferTagSerializer;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import cern.c2mon.shared.daq.republisher.Publisher;
import cern.c2mon.shared.daq.republisher.Republisher;
import cern.c2mon.shared.daq.republisher.RepublisherFactory;
import cern.c2mon.shared.util.jms.JmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

/**
 * This class implements the <code>AlarmAggregatorListener</code>
 * interface for sending tag value updates to the tag JMS destination
 * topics. The update information is transmitted as GSON message
 * with the <code>TransferTagValue</code> class.
 * <p>
 * This class implements the <code>ConfigurationUpdateListener</code>
 * interface for sending configuration updates to the tag JMS destination
 * topics. The update information is transmitted as GSON message
 * with the <code>TransferTag</code> class.
 *
 * @author Matthias Braeger, Mark Brightwell, Ignacio Vilches
 * @see AlarmAggregatorListener
 * @see ConfigurationUpdateListener
 * @see TagValueUpdate
 */
@Slf4j
@Service
@ManagedResource(description = "Bean publishing tag updates to the clients")
public class TagValuePublisher implements AlarmAggregatorListener, ConfigurationUpdateListener, Publisher<TagWithAlarms> {

    /**
     * Bean providing for sending JMS messages and waiting for a response
     */
    private final JmsSender jmsSender;

    /**
     * Listens for Configuration changes
     */
    private final ConfigurationUpdate configurationUpdate;

    /**
     * Listens for Tag updates, evaluates all associated alarms and passes the result
     */
    private final AlarmAggregator alarmAggregator;

    /**
     * Contains re-publication logic
     */
    private Republisher<TagWithAlarms> republisher;

    /**
     * Time between republicaton attempts
     */
    private int republicationDelay;

    /**
     * Reference to the tag facade gateway to retrieve a tag copies with the
     * associated alarms
     */
    private final AlarmService alarmService;

    /**
     * Reference to the tag location service
     */
    private TagCacheCollection tagLocationService;

    /**
     * Used to determine, whether a given tag is an AliveTag
     */
    private AliveTagService aliveTimerFacade;

    private ClientProperties properties;

    /**
     * Default Constructor
     *
     * @param jmsSender           Used for sending JMS messages and waiting for a response
     * @param aliveTimerFacade    Used to determine, whether a given tag is an AliveTag
     * @param alarmAggregator     Used to register this <code>AlarmAggregatorListener</code>
     * @param configurationUpdate Used to register this <code>ConfigurationUpdateListener</code>
     * @param alarmService        Reference to the tag facade gateway singleton
     * @param tagLocationService  Reference to the tag location service
     */
    @Autowired
    public TagValuePublisher(@Qualifier("clientTopicPublisher") final JmsSender jmsSender,
                             final AlarmAggregator alarmAggregator,
                             final AliveTagService aliveTimerFacade,
                             final ConfigurationUpdate configurationUpdate,
                             final AlarmService alarmService,
                             final TagCacheCollection tagLocationService,
                             final ClientProperties properties) {
        this.aliveTimerFacade = aliveTimerFacade;
        this.jmsSender = jmsSender;
        this.alarmAggregator = alarmAggregator;
        this.configurationUpdate = configurationUpdate;
        this.alarmService = alarmService;
        this.tagLocationService = tagLocationService;
        this.republisher = RepublisherFactory.createRepublisher(this, "Tag");
        this.properties = properties;
    }

    /**
     * Init method registering this listener to the <code>AlarmAggregator</code>.
     */
    @PostConstruct
    public void init() {
        log.info("init - Starting Tag publisher.");
        log.trace("init - Registering for Tag Updates.");

        this.alarmAggregator.registerForTagUpdates(this);

        log.trace("init - Registering for Configuration Updates.");

        this.configurationUpdate.registerForConfigurationUpdates(this);

        if (republicationDelay != 0)
            republisher.setRepublicationDelay(republicationDelay);
        republisher.start();
    }

    /**
     * Before shutdown, stop republisher thread.
     */
    @PreDestroy
    public void shutdown() {
        log.info("shutdown - Stopping tag publisher.");
        republisher.stop();
    }

    /**
     * Generates for every notification a <code>TransferTagValue</code>
     * object which is then sent as serialized GSON message trough the
     * dedicated JMS client tag topic.
     *
     * @param tag    the updated tag
     * @param alarms the new values of the associated alarms; this list
     *               is null if no alarms are associated to the tag
     */
    @Override
    public void notifyOnUpdate(Tag tag, List<Alarm> alarms) {
        TagWithAlarms tagWithAlarms = new TagWithAlarms<>(tag, alarms);
        try {
            publish(tagWithAlarms);
        } catch (JmsException e) {
            log.error("notifyOnUpdate - Error publishing tag update to topic for tag " + tagWithAlarms.getTag().getId() + " - submitting for republication", e);
            republisher.publicationFailed(tagWithAlarms);
        }
    }

    @Override
    public void publish(final TagWithAlarms tagWithAlarms) {
        TransferTagValueImpl tagValue = TransferObjectFactory.createTransferTagValue(tagWithAlarms);
        log.trace("publish - Publishing tag update to client: " + TransferTagSerializer.toJson(tagValue));

        jmsSender.sendToTopic(TransferTagSerializer.toJson(tagValue), TopicProvider.topicFor(tagWithAlarms.getTag(), properties));
    }

    @Override
    public void notifyOnConfigurationUpdate(Long tagId) {
        TagWithAlarms tagWithAlarms = alarmService.getTagWithAlarmsAtomically(tagId);
        try {
            String topic = TopicProvider.topicFor(tagWithAlarms.getTag(), properties);
            TransferTagImpl tag = TransferObjectFactory.createTransferTag(tagWithAlarms, aliveTimerFacade.isRegisteredAliveTimer(tagId), topic);

            log.trace("notifyOnConfigurationUpdate - Publishing configuration update to client: " + TransferTagSerializer.toJson(tag));

            jmsSender.sendToTopic(TransferTagSerializer.toJson(tag), topic);
        } catch (JmsException e) {
            log.error("notifyOnConfigurationUpdate - Error publishing configuration update to topic for tag " + tagWithAlarms.getTag().getId()
                    + " - submitting for republication", e);
            republisher.publicationFailed(tagWithAlarms);
        }
    }

    /**
     * @return the total number of failed publications since the publisher start
     */
    @ManagedOperation(description = "Returns the total number of failed publication attempts since the application started")
    public long getNumberFailedPublications() {
        return republisher.getNumberFailedPublications();
    }

    /**
     * @return the number of current tag updates awaiting publication to the clients
     */
    @ManagedOperation(description = "Returns the current number of events awaiting re-publication (should be 0 in normal operation)")
    public int getSizeUnpublishedList() {
        return republisher.getSizeUnpublishedList();
    }
}
