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
package cern.c2mon.server.supervision.alive;

import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.server.common.config.ServerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Timer;

/**
 * Timer that regularly checks all the active alive timers monitoring
 * the connections to the DAQs, Equipment and SubEquipment.
 *
 * This component used to take precautions to ensure it only runs on
 * one server. There has been a functionality change with the cache
 * refactoring of 2020 and later versions of this component do not
 * take such measures. This component will run on any C2MON servers
 *
 * <p>Notice that an alive timer is considered expired when alive-interval
 *  + alive-interval/3 milliseconds have expired since the last alive
 *  message arrived, where alive-interval is specific to the AliveTimer
 *  object (see <code>hasExpired</code> in {@link AliveTagService}).
 *
 * @see AliveTagChecker
 * @author Alexandros Papageorgiou, Mark Brightwell
 */
@Named
@Singleton
public class AliveTagChecker implements SmartLifecycle {

  /**
   * Log4j Logger for this class.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(AliveTagChecker.class);

  /**
   * How often the timer checks whether the alive
   * timer have expired.
   */
  private static final int SCAN_INTERVAL = 10000;

  /**
   * The time the server waits before doing first
   * checks at start up (this gives time for incoming
   * alives to be processed).
   */
  private static final int INITIAL_SCAN_DELAY = 120000;

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  /**
   * Timer object
   */
  private Timer timer;

  private AliveTagCheckerTask aliveTagCheckerTask;

  /**
   *
   *
   * @param aliveTagCheckerTask the actual task that will run every
   *        {@link AliveTagChecker#SCAN_INTERVAL} millis
   */
  @Inject
  public AliveTagChecker(AliveTagCheckerTask aliveTagCheckerTask) {
    this.aliveTagCheckerTask = aliveTagCheckerTask;
  }

  /**
   * Starts the timer. Alive timers will be checked from then on.
   */
  @Override
  public synchronized void start() {
    LOGGER.info("Starting the C2MON alive timer mechanism.");
    timer = new Timer("AliveChecker");
    timer.schedule(aliveTagCheckerTask, INITIAL_SCAN_DELAY, SCAN_INTERVAL);
    running = true;
  }

  /**
   * Stops the timer mechanism. No more checks are made on the
   * alive timers.
   *
   * <p>Can be restarted using the start method.
   */
  @Override
  public synchronized void stop() {
    LOGGER.info("Stopping the C2MON alive timer mechanism.");
    timer.cancel();
    running = false;
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
  public synchronized boolean isRunning() {
   return running;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST + 1;
  }
}
