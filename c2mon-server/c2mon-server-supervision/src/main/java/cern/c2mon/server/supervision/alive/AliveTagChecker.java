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
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.supervision.SupervisionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

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
 * @author Mark Brightwell
 *
 */
@Named
@Singleton
public class AliveTagChecker extends TimerTask implements SmartLifecycle {

  /**
   * Log4j Logger for this class.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(AliveTagChecker.class);

  /**
   * SMS logger for warnings.
   */
  private static final Logger SMS_LOGGER = LoggerFactory.getLogger("AdminSmsLogger");

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

  /**
   * Reference to alive timer facade.
   */
  private final AliveTagService aliveTimerService;

  /**
   * Reference to alive timer cache.
   */
  private C2monCache<AliveTag> aliveTimerCache;

  /**
   * Reference to the SupervisionManager bean.
   */
  private final SupervisionManager supervisionManager;

  /**
   * Threshold of DAQ/Equipment/SubEqu. down when warning is sent to admin.
   */
  private static final short WARNING_THRESHOLD = 50;

  /**
   * Warning has been sent.
   */
  private boolean alarmActive = false;

  /**
   * Count down to alarm switch off.
   */
  private AtomicInteger warningSwitchOffCountDown = new AtomicInteger(SWITCH_OFF_COUNTDOWN);

  /**
   * 10mins, because we decrement this once per scan, so time value is 60 * SCAN_INTERVAL
   */
  private static final int SWITCH_OFF_COUNTDOWN = 60;

  private static long lastCheck = 0L;

  /**
   * @param aliveTimerService the alive timer facade bean
   * @param supervisionManager the supervision manager bean
   */
  @Inject
  public AliveTagChecker(AliveTagService aliveTimerService, SupervisionManager supervisionManager) {
    this.aliveTimerService = aliveTimerService;
    this.aliveTimerCache = aliveTimerService.getCache();
    this.supervisionManager = supervisionManager;
  }

  /**
   * Starts the timer. Alive timers will be checked from then on.
   */
  @Override
  public synchronized void start() {
    LOGGER.info("Starting the C2MON alive timer mechanism.");
    timer = new Timer("AliveChecker");
    timer.schedule(this, INITIAL_SCAN_DELAY, SCAN_INTERVAL);
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

  /**
   * Run method of the AliveTimerManager thread.
   */
  @Override
  public void run() {
    if (System.currentTimeMillis() - lastCheck < 9000) {
      LOGGER.debug("Skipping alive check as already performed.");
    }

    LOGGER.debug("run() : checking alive timers ... ");

    try {
      long aliveDownCount = calculateAliveDownAndNotify();

      sendNotificationsIfRequired(aliveDownCount);
    } catch (Exception e) {
      LOGGER.error("Unexpected exception when checking the alive timers", e);
    }
    lastCheck = System.currentTimeMillis();

    LOGGER.debug("run() : finished checking alive timers ... ");
  }

  private long calculateAliveDownAndNotify() {
    return aliveTimerCache.getKeys()
      // Potentially optimizable by switching to single stream
      .parallelStream()
      .filter(this::isExpired)
      .peek(supervisionManager::onAliveTimerExpiration)
      .count();
  }

  private boolean isExpired(long id) {
    boolean aliveExpired = false;

    if (aliveTimerCache.containsKey(id)) {
      try {
        if (aliveTimerService.hasExpired(id)) {
          aliveTimerService.stop(id, System.currentTimeMillis());
          aliveExpired = true;
        }
      } catch (CacheElementNotFoundException notFound) {
        LOGGER.warn("Failed to locate alive timer in cache on expiration check (may happen exceptionally if just removed).", notFound);
      }
    } else {
      aliveExpired = true;
    }

    return aliveExpired;
  }

  private void sendNotificationsIfRequired(long aliveDownCount) {
    if (!alarmActive && aliveDownCount > WARNING_THRESHOLD) {
      alarmActive = true;
      LOGGER.warn("Over {} DAQ/Equipment are currently down.", WARNING_THRESHOLD);
      SMS_LOGGER.warn("Over {} DAQ/Equipment are currently down.", WARNING_THRESHOLD);
    } else if (alarmActive && warningSwitchOffCountDown.decrementAndGet() == 0) {
      SMS_LOGGER.warn("DAQ/Equipment status back to normal ({} detected as down)", aliveDownCount);
      alarmActive = false;
      warningSwitchOffCountDown = new AtomicInteger(SWITCH_OFF_COUNTDOWN);
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
  public synchronized boolean isRunning() {
   return running;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST + 1;
  }
}
