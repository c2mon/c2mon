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
package cern.c2mon.server.client.heartbeat.impl;

import cern.c2mon.cache.actions.oscillation.OscillationService;
import cern.c2mon.server.client.heartbeat.HeartbeatListener;
import cern.c2mon.server.client.heartbeat.HeartbeatManager;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.shared.client.supervision.Heartbeat;
import cern.c2mon.shared.util.jms.JmsSender;
import cern.c2mon.shared.util.json.GsonFactory;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The HeartbeatManager bean generates regular server heartbeats, which
 * can then be used by clients for monitoring the alive status of the
 * server.
 *
 * <p>The heartbeat is published directly on a JMS topic. Server modules can
 * also register as listeners for internal heartbeat notifications.
 *
 * @author Mark Brightwell
 */
@Slf4j
@Named
@Singleton
public class HeartbeatManagerImpl implements HeartbeatManager, SmartLifecycle {

  /**
   * Gson that is reused.
   */
  private Gson gson = GsonFactory.createGson();

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  /**
   * Timer object used for generating heartbeat messages at fixed intervals.
   */
  private Timer timer;

  /**
   * Heart beat interval. Default is 30000.
   */
  private long heartbeatInterval = Heartbeat.getHeartbeatInterval();

  /**
   * Bean for sending JMS heartbeat messages.
   */
  private JmsSender heartbeatSender;

  /**
   * The list of listeners registered for heartbeats.
   * Synchronise on this field when accessing this list.
   */
  private List<HeartbeatListener> listeners;

  /**
   * Is only accessed to check cache connectivity.
   */
  private OscillationService oscillationService;

  private static final String TIMER_NAME_PREFIX = "Heartbeat";
  /**
   * Constructor.
   * @param heartbeatSender the JmsSender for sending heartbeats to the clients
   */
  @Autowired
  public HeartbeatManagerImpl(@Qualifier("heartbeatSender") final JmsSender heartbeatSender,
                              final OscillationService oscillationService) {
    super();
    this.heartbeatSender = heartbeatSender;
    this.timer = new Timer(TIMER_NAME_PREFIX);
    this.listeners = new ArrayList<>();
    this.oscillationService = oscillationService;
  }

  @Override
  public void registerToHeartbeat(final HeartbeatListener heartbeatListener) {
    synchronized (listeners) {
      listeners.add(heartbeatListener);
    }
  }

  /**
   * Notifies all the registered listeners of the heartbeat.
   * @param heartbeat the new heartbeat
   */
  private void notifyListeners(Heartbeat heartbeat) {
    synchronized (listeners) {
      for (HeartbeatListener listener : listeners) {
        listener.notifyHeartbeat(heartbeat);
      }
    }
  }

  /**
   * Overrides the default heartbeat interval.
   *
   * @param heartbeatInterval the heartbeatInterval to set
   */
  public void setHeartbeatInterval(long heartbeatInterval) {
    this.heartbeatInterval = heartbeatInterval;
  }


  /**
   * Bean is started up manually.
   */
  @Override
  public boolean isAutoStartup() {
    return true;
  }

  /**
   * Stops the heart beat and calls
   * the callback.
   */
  @Override
  public void stop(Runnable callback) {
    stop();
    callback.run();
  }

  /**
   * Is the heartbeat currently switched on?
   */
  @Override
  public synchronized boolean isRunning() {
    return running;
  }

  /**
   * Start sending the heartbeat.
   */
  @Override
  public synchronized void start() {
    log.info("Starting server heartbeat.");
    this.timer.scheduleAtFixedRate(new HeartbeatTask(), 0, heartbeatInterval);
    running = true;
  }

  /**
   * Stops heartbeat sending.
   */
  @Override
  public synchronized void stop() {
    log.debug("Stopping server heartbeat.");
    this.timer.cancel();
    running = false;
  }

  /**
   * Started once everything else is started (client use this signal
   * and will expect rapid response).
   */
  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST + 1;
  }

  /**
   * The task that is run at every heartbeat.
   * @author Mark Brightwell
   *
   */
  private class HeartbeatTask extends TimerTask {

    /**
     * Sends the server heart beat to all heartbeat listeners.
     */
    @Override
    public void run() {
      if (log.isDebugEnabled()) {
        log.debug("Sending server heartbeat.");
      }
      try {
        final Heartbeat heartbeat = new Heartbeat();
        //access cache to check cache process is responding when using distributed cache
        oscillationService.getLastOscillationCheck();
        heartbeatSender.send(gson.toJson(heartbeat));
        notifyListeners(heartbeat);
      } catch (Exception e) {
        log.error("run() : Error sending heartbeat message.", e);
      }
    }

  }

}
