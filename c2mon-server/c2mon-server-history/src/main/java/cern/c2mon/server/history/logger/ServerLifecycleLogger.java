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
package cern.c2mon.server.history.logger;

import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.config.ServerProperties;
import cern.c2mon.server.history.mapper.ServerLifecycleEventMapper;
import cern.c2mon.shared.client.lifecycle.LifecycleEventType;
import cern.c2mon.shared.client.lifecycle.ServerLifecycleEvent;
import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Bean listening for server stops/starts and logging
 * them in the database.
 *
 * <p>Will always log start events if the DB is available at the time. If not available,
 * thread will retry until DB is available or server shutdown.
 *
 * <p>Stop events will be missing if the server is kill -9'd, or if the DB is not available
 * at shutdown of the server.
 *
 * @author Mark Brightwell
 *
 */
@Component
public class ServerLifecycleLogger implements SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerLifecycleLogger.class);

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  /**
   * Name of this server.
   */
  private String serverName;

  /**
   * Mapper bean for logging events.
   */
  private ServerLifecycleEventMapper serverLifecycleEventMapper;

  /**
   * Timer for retrying the start log if unsuccessful.
   */
  private Timer relogTimer;

  /**
   * Time between re-log attempts of server start event.
   */
  private long timeBetweenRelogs = 120000;

  /**
   * Constructor.
   * @param serverLifecycleEventMapper the mapper bean used for writing to the DB
   */
  @Inject
  public ServerLifecycleLogger(final ServerLifecycleEventMapper serverLifecycleEventMapper, ServerProperties properties) {
    this.serverLifecycleEventMapper = serverLifecycleEventMapper;
    this.serverName = properties.getName();
  }

  private void logStartEvent() {
    final ServerLifecycleEvent startEvent = new ServerLifecycleEvent(new Timestamp(System.currentTimeMillis()), serverName, LifecycleEventType.START);
    logStartEvent(startEvent);
  }

  private synchronized void logStartEvent(final ServerLifecycleEvent event) {
    try {
      serverLifecycleEventMapper.logEvent(event);
      if (relogTimer != null){
        relogTimer.cancel();
      }
    } catch (PersistenceException e) {
      LOGGER.error("Exception caught when logging start event, will try again in 2 minutes time: {}", e.getMessage());
      if (relogTimer == null){
        relogTimer = new Timer("Lifecycle-start-log-timer");
      }
      if (running) {
        relogTimer.schedule(new TimerTask() {
          @Override
          public void run() {
            logStartEvent(event);
          }
       }, timeBetweenRelogs);
      }
    }
  }

  private void logStopEvent() {
    try {
      serverLifecycleEventMapper.logEvent(new ServerLifecycleEvent(new Timestamp(System.currentTimeMillis()), serverName, LifecycleEventType.STOP));
    } catch (PersistenceException e) {
      LOGGER.error("Exception caught when logging server stop event: {}", e.getMessage());
    }
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable arg0) {
    stop();
    arg0.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    running = true;
    logStartEvent();
  }

  @Override
  public synchronized void stop() {
    running = false;
    if (relogTimer != null){
      relogTimer.cancel();
    }
    logStopEvent();
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST - 1;
  }

  /**
   * @param serverLifecycleEventMapper the serverLifecycleEventMapper to set
   */
  public void setServerLifecycleEventMapper(ServerLifecycleEventMapper serverLifecycleEventMapper) {
    this.serverLifecycleEventMapper = serverLifecycleEventMapper;
  }

  /**
   * @param timeBetweenRelogs the timeBetweenRelogs to set
   */
  public void setTimeBetweenRelogs(long timeBetweenRelogs) {
    this.timeBetweenRelogs = timeBetweenRelogs;
  }

}
