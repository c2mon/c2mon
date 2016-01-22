/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.eslog.listener;

import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.eslog.logger.SupervisionIndexer;
import cern.c2mon.server.eslog.structure.converter.SupervisionESConverter;
import cern.c2mon.server.eslog.structure.types.AlarmES;
import cern.c2mon.server.eslog.structure.types.SupervisionES;
import cern.c2mon.server.supervision.SupervisionListener;
import cern.c2mon.server.supervision.SupervisionNotifier;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
/**
 * Listens for supervision notifications and send them to ElasticSearch.
 * @author Alban Marguet
 */
@Slf4j
@Service
public class ESLogSupervisionListener implements SupervisionListener, SmartLifecycle {
  /** Notifier of supervision module. */
  private SupervisionNotifier supervisionNotifier;

  /** Supervision Mapper */
  private SupervisionIndexer supervisionIndexer;

  /** Convert SupervisionEvent to SupervisionES for ElasticSearch.*/
  private SupervisionESConverter supervisionESConverter;

  /** Listener container lifecycle hook. */
  private Lifecycle listenerContainer;

  /** Lifecycle flag. */
  private volatile boolean running = false;

  /**
   * Autowired constructor.
   *
   * @param supervisionNotifier the notifier to register to
   * @param supervisionIndexer the mapper to write to the DB
   */
  @Autowired
  public ESLogSupervisionListener(final SupervisionNotifier supervisionNotifier, final SupervisionIndexer supervisionIndexer, final SupervisionESConverter supervisionESConverter) {
    this.supervisionNotifier = supervisionNotifier;
    this.supervisionIndexer = supervisionIndexer;
    this.supervisionESConverter = supervisionESConverter;
  }

  /**
   * Called at bean initialisation. Registers for
   * notifications.
   */
  @PostConstruct
  public void init() {
    log.debug("Registering ElasticSearch module for supervision updates");
    listenerContainer = supervisionNotifier.registerAsListener(this);
  }

  @Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
  @Override
  public void notifySupervisionEvent(final SupervisionEvent supervisionEvent) {
    if (log.isDebugEnabled()) {
      log.debug("notifySupervisionEvent() - Logging supervision status " + supervisionEvent.getStatus()
          + " for " + supervisionEvent.getEntity()
          + " " + supervisionEvent.getEntityId() + " to ElasticSearch");
    }
    SupervisionES supervisionES = supervisionESConverter.convertSupervisionEventToSupervisionES(supervisionEvent);
    supervisionIndexer.logSupervisionEvent(supervisionES);
  }

  @Override
  public boolean isAutoStartup() {
    return false;
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
    log.debug("Starting ES supervision event logger.");
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    log.debug("Stopping ES supervision event logger.");
    listenerContainer.stop();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;
  }
}