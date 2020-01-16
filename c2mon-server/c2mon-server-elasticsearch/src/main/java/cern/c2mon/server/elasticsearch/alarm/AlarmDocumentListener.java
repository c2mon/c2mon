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
package cern.c2mon.server.elasticsearch.alarm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;

/**
 * Listens for {@link Alarm} updates and converts them to {@link AlarmDocument}
 * instances before sending them to the {@link IPersistenceManager} responsible
 * for indexing them.
 *
 * @author Alban Marguet
 */
@Slf4j
@Component
public class AlarmDocumentListener implements C2monCacheListener<Alarm>, SmartLifecycle {

  private final ElasticsearchProperties properties;

  @Qualifier("alarmDocumentPersistenceManager")
  private final IPersistenceManager<AlarmDocument> persistenceManager;

  private final AlarmValueDocumentConverter converter;

  private Lifecycle listenerContainer;

  private volatile boolean running = false;

  @Autowired
  public AlarmDocumentListener(ElasticsearchProperties properties, CacheRegistrationService cacheRegistrationService, IPersistenceManager<AlarmDocument> persistenceManager, AlarmValueDocumentConverter converter) {
    this.properties = properties;
    this.persistenceManager = persistenceManager;
    this.converter = converter;
    if (properties.isEnabled()) {
      listenerContainer = cacheRegistrationService.registerToAlarms(this);
    }
  }

  @Override
  public void notifyElementUpdated(final Alarm alarm) {
    if (alarm == null) {
      log.warn("Received a null alarm");
      return;
    }

    persistenceManager.storeData(converter.convert(alarm));
  }

  @Override
  public void confirmStatus(Alarm alarm) {
    // logic not required
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
    if (properties.isEnabled()) {
      running = true;
      listenerContainer.start();
    }
  }

  @Override
  public void stop() {
    if (properties.isEnabled()) {
      listenerContainer.stop();
      running = false;
    }

  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;
  }
}
