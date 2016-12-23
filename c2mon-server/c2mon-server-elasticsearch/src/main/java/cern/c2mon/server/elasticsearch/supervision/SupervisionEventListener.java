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
package cern.c2mon.server.elasticsearch.supervision;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.supervision.SupervisionListener;
import cern.c2mon.server.supervision.SupervisionNotifier;
import cern.c2mon.shared.client.supervision.SupervisionEvent;

/**
 * Listens for supervision notifications and sends the corresponding
 * {@link SupervisionEventDocument} to Elasticsearch.
 *
 * @author Alban Marguet
 */
@Slf4j
@Service
public class SupervisionEventListener implements SupervisionListener, SmartLifecycle {

  @Autowired
  private SupervisionNotifier supervisionNotifier;

  @Autowired
  @Qualifier("supervisionEventDocumentPersistenceManager")
  private IPersistenceManager<SupervisionEventDocument> persistenceManager;

  @Autowired
  private SupervisionEventDocumentConverter converter;

  private Lifecycle listenerContainer;

  private volatile boolean running = false;

  @PostConstruct
  public void init() {
    listenerContainer = supervisionNotifier.registerAsListener(this);
  }

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
  @Override
  public void notifySupervisionEvent(final SupervisionEvent supervisionEvent) {
    if (supervisionEvent == null) {
      log.warn("Received a null supervision event");
      return;
    }

    log.debug("Indexing supervision event {} for entity {} (#{})",
        supervisionEvent.getStatus(), supervisionEvent.getEntity(), supervisionEvent.getEntityId());

    persistenceManager.storeData(converter.convert(supervisionEvent));
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
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    listenerContainer.stop();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;
  }
}
