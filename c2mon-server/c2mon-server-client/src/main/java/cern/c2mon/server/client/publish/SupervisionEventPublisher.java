/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.jms.JmsException;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.supervision.SupervisionListener;
import cern.c2mon.server.supervision.SupervisionNotifier;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.daq.lifecycle.Lifecycle;
import cern.c2mon.shared.daq.republisher.Publisher;
import cern.c2mon.shared.daq.republisher.Republisher;
import cern.c2mon.shared.daq.republisher.RepublisherFactory;
import cern.c2mon.shared.util.jms.JmsSender;
import cern.c2mon.shared.util.json.GsonFactory;

/**
 * Publishes supervision events to the C2MON clients. If JMS exceptions occur,
 * re-publication will be attempted until successful.
 *
 * @author Mark Brightwell
 *
 */
@Service
@ManagedResource(description = "Bean publishing supervision updates to the clients")
public class SupervisionEventPublisher implements SupervisionListener, SmartLifecycle, Publisher<SupervisionEvent> {

  /** Class logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(SupervisionEventPublisher.class);

  /** Bean providing for sending JMS messages and waiting for a response; default destination set */
  private final JmsSender jmsSender;

  /** Reference to the <code>SupervisionNotifier</code> for registering this listener */
  private final SupervisionNotifier supervisionNotifier;

  /** Contains re-publication logic */
  private Republisher<SupervisionEvent> republisher;

  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();

  /** Listener container lifecycle hook */
  private Lifecycle listenerContainer;

  /** Lifecycle flag */
  private volatile boolean running = false;

  /**
   * Default Constructor
   * @param pJmsSender Used for sending JMS messages and waiting for a response
   * @param pSupervisionNotifier Used for registering this class as listener
   */
  @Autowired
  public SupervisionEventPublisher(@Qualifier("supervisionTopicPublisher") final JmsSender pJmsSender,
                                   final SupervisionNotifier pSupervisionNotifier) {
    jmsSender = pJmsSender;
    supervisionNotifier = pSupervisionNotifier;
    republisher = RepublisherFactory.createRepublisher(this, "Supervision Event");
  }


  /**
   * Init method registering this listener to the <code>TimCache</code>.
   */
  @PostConstruct
  public void init() {
    listenerContainer = supervisionNotifier.registerAsListener(this);
  }

  @Override
  public void notifySupervisionEvent(final SupervisionEvent supervisionEvent) {
    try {
      publish(supervisionEvent);
    } catch (JmsException e) {
      LOGGER.error("Error publishing supervision event - submitting for republication", e);
      republisher.publicationFailed(supervisionEvent);
    }
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable runnable) {
    stop();
    runnable.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    LOGGER.debug("Starting supervision event publisher");
    running = true;
    republisher.start();
    listenerContainer.start();
  }

  @Override
  public void stop() {
    LOGGER.debug("Stopping supervision event publisher");
    listenerContainer.stop();
    republisher.stop();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;
  }


  @Override
  public void publish(SupervisionEvent supervisionEvent) {
    LOGGER.debug("Publishing supervision event: " + GSON.toJson(supervisionEvent));
    jmsSender.send(GSON.toJson(supervisionEvent));
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
