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
package cern.c2mon.server.eslog.listener;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.pmanager.persistence.impl.TimPersistenceManager;
import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.eslog.logger.AlarmIndexer;
import cern.c2mon.server.eslog.structure.converter.AlarmESLogConverter;
import cern.c2mon.server.eslog.structure.types.AlarmES;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Listens to updates in the Alarm cache and send them to ElasticSearch with the AlarmIndexer class.
 *
 * @author Alban Marguet
 */
@Slf4j
@Service
public class AlarmESLogListener implements C2monCacheListener<Alarm>, SmartLifecycle {
  /** Reference to registration service. */
  private CacheRegistrationService cacheRegistrationService;

  /** Bean that logs Tags into ElasticSearch. */
  private IPersistenceManager persistenceManager;

  /** Allows to get the right information from the Alarm to create an AlarmES instance. */
  private AlarmESLogConverter alarmESLogConverter;

  /** Listener container lifecycle hook. */
  private Lifecycle listenerContainer;

  /** Lifecycle flag. */
  private volatile boolean running = false;

  /**
   * Autowired constructor.
   *
   * @param cacheRegistrationService for registering cache listeners.
   * @param persistenceManager for logging cache objects to ElasticSearch.
   */
  @Autowired
  public AlarmESLogListener(final CacheRegistrationService cacheRegistrationService, @Qualifier("alarmESPersistenceManager") final IPersistenceManager persistenceManager, final AlarmESLogConverter alarmESLogConverter) {
    super();
    this.cacheRegistrationService = cacheRegistrationService;
    this.persistenceManager = persistenceManager;
    this.alarmESLogConverter = alarmESLogConverter;
  }

  /**
   * Registers to be notified of all Tag updates (data, rule and control tags).
   */
  @PostConstruct
  public void init() {
    listenerContainer = cacheRegistrationService.registerToAlarms(this);
  }

  @Override
  public void notifyElementUpdated(Alarm cacheable) {
    log.debug("notifyElementUpdated() - Received an Alarm event.");
    try {
      AlarmES alarmES = alarmESLogConverter.convertAlarmToAlarmES(cacheable);
      sendIfAlarmESIsNotNull(alarmES);
    }
    catch(Exception e) {
      log.error("notifyElementUpdated() - Could not add Alarm to ElasticSearch: Alarm # " + cacheable.getId() + ".", e);
    }
  }

  public void sendIfAlarmESIsNotNull(AlarmES alarmES) {
    if (alarmES != null) {
      persistenceManager.storeData(alarmES);
    }
    else {
      log.warn("notifyElementUpdated() - Warning: The received alarm was null.");
    }
  }

  @Override
  public void confirmStatus(Alarm cacheable) {
    // no confirmation required
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
    log.debug("Starting Alarm logger (eslog)");
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    log.debug("Stopping Alarm logger (eslog)");
    listenerContainer.stop();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;
  }
}