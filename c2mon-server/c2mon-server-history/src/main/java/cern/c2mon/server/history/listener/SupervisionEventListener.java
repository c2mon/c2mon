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
package cern.c2mon.server.history.listener;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.history.mapper.SupervisionEventMapper;
import cern.c2mon.server.supervision.SupervisionListener;
import cern.c2mon.server.supervision.SupervisionNotifier;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.daq.lifecycle.Lifecycle;

/**
 * Listens for supervision notifications from the core and logs
 * these into the history database.
 *
 * @author Mark Brightwell
 */
@Service
public class SupervisionEventListener implements SupervisionListener, SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SupervisionEventListener.class);

  /**
   * Notifier of supervision module.
   */
  private SupervisionNotifier supervisionNotifier;

  /**
   * Supervision Mapper
   */
  private SupervisionEventMapper supervisionEventMapper;

  /**
   * Listener container lifecycle hook.
   */
  private Lifecycle listenerContainer;

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  /**
   * Autowired constructor.
   *
   * @param supervisionNotifier the notifier to register to
   * @param supervisionEventMapper the mapper to write to the DB
   */
  @Autowired
  public SupervisionEventListener(final SupervisionNotifier supervisionNotifier, final SupervisionEventMapper supervisionEventMapper) {
    super();
    this.supervisionNotifier = supervisionNotifier;
    this.supervisionEventMapper = supervisionEventMapper;
  }

  /**
   * Called at bean initialisation. Registers for
   * notifications.
   */
  @PostConstruct
  public void init() {
    LOGGER.debug("Registering history module for supervision updates");
    listenerContainer = supervisionNotifier.registerAsListener(this);
  }

  @Transactional(propagation=Propagation.REQUIRED,isolation=Isolation.DEFAULT)
  @Override
  public void notifySupervisionEvent(final SupervisionEvent supervisionEvent) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Logging supervision status " + supervisionEvent.getStatus()
            + " for " + supervisionEvent.getEntity()
            + " " + supervisionEvent.getEntityId());
    }
    supervisionEventMapper.logSupervisionEvent(supervisionEvent);
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
    LOGGER.debug("Starting supervision event logger.");
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    LOGGER.debug("Stopping supervision event logger.");
    listenerContainer.stop();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;
  }

}
