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

import cern.c2mon.cache.actions.alivetimer.AliveTimerService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.supervision.SupervisionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Timer that regularly checks all the active alive timers monitoring
 * the connections to the DAQs, Equipment and SubEquipment.
 *
 * <p>Notice that an alive timer is considered expired when alive-interval
 *  + alive-interval/3 milliseconds have expired since the last alive
 *  message arrived, where alive-interval is specific to the AliveTimer
 *  object (see <code>hasExpired</code> in {@link AliveTimerService}).
 *
 * @author Mark Brightwell
 *
 */
@Service
public class AliveTimerChecker extends TimerTask implements SmartLifecycle {

  /**
   * Log4j Logger for this class.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(AliveTimerChecker.class);

  /**
   * SMS logger for warnings.
   */
  private static final Logger SMS_LOGGER = LoggerFactory.getLogger("AdminSmsLogger");

  /**
   * Cluster cache key to ensure that a server does not try to access
   * LAST_ALIVE_TIMER_CHECK_LONG during instantiation if it already exists. This
   * is because a cache loading blockage can happen if a server holds
   * LAST_ALIVE_TIMER_CHECK_LONG while another server starts up. See
   * https://issues.cern.ch/browse/TIMS-1037.
   */
  private static final String LAST_ALIVE_TIMER_CHECK_INITIALISATION_KEY = "c2mon.supervision.AliveTimerChecker.lastAliveTimerCheckInitialisationKey";

  /**
   * Cluster cache key for retrieving the time of last check
   * of the alives. Across server cluster it assures that
   * the alive check only takes place on a single server.
   */
  private static final String LAST_ALIVE_TIMER_CHECK_LONG = "c2mon.supervision.AliveTimerChecker.lastAliveTimerCheck";

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
  private AliveTimerService aliveTimerService;

  /**
   * Reference to alive timer cache.
   */
  private C2monCache<AliveTag> aliveTimerCache;

  /**
   * Reference to the SupervisionManager bean.
   */
  private SupervisionManager supervisionManager;

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

  private static final int SWITCH_OFF_COUNTDOWN = 60; //10mins

  /**
   * @param aliveTimerService the alive timer facade bean
   * @param supervisionManager the supervision manager bean
   */
  @Autowired
  public AliveTimerChecker(final AliveTimerService aliveTimerService,
                           final SupervisionManager supervisionManager) {
    super();
    this.aliveTimerService = aliveTimerService;
    this.aliveTimerCache = aliveTimerService.getCache();
    this.supervisionManager = supervisionManager;
  }

  /**
   * Initialises the clustered values
   */
  @PostConstruct
  public void init() {
    LOGGER.trace("Initialising AliveTimerChecker...");
    // TODO (Alex) Replace this logic
//    clusterCache.acquireWriteLockOnKey(LAST_ALIVE_TIMER_CHECK_INITIALISATION_KEY);
//    try {
//      if (!clusterCache.hasKey(LAST_ALIVE_TIMER_CHECK_INITIALISATION_KEY)) {
//        clusterCache.put(LAST_ALIVE_TIMER_CHECK_INITIALISATION_KEY, true);
//        clusterCache.put(LAST_ALIVE_TIMER_CHECK_LONG, 0L);
//      }
//    } finally {
//      clusterCache.releaseWriteLockOnKey(LAST_ALIVE_TIMER_CHECK_INITIALISATION_KEY);
//    }
    LOGGER.trace("Initialisation complete.");
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
  // TODO (Alex) Review this logic
  //TODO: create additional cache with ExpirePolicy enabled, thanks to that we would be able to replace current logic inside run() and check if aliveTimerCacheObject has expired
  //TODO: it should work like this:
  //TODO: if aliveTimer expires insdie of AliveTimerExpireCache(doesnt exist yet), then aliveTimer inside AliveTimerCache should be updated
  @Override
  public void run() {
//    Long lastCheck = (Long) clusterCache.getCopy(LAST_ALIVE_TIMER_CHECK_LONG);
    long lastCheck = 0L;
    if (System.currentTimeMillis() - lastCheck < 9000) { //results in check on a single server
      LOGGER.debug("Skipping alive check as already performed.");
    } else {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("run() : checking alive timers ... ");
      }
      short aliveDownCount = 0;
      try {
        for (Long currentId : aliveTimerCache.getKeys()) {
          AliveTag aliveTimerCopy = null;
          boolean aliveExpiration = false;
          aliveTimerCopy = aliveTimerCache.get(currentId);

          if (aliveTimerCopy.getValue()) {
            if (aliveTimerService.hasExpired(currentId)) {
              aliveTimerService.stop(currentId, System.currentTimeMillis());
              aliveExpiration = true;
              aliveDownCount++;
            }
          } else {
            aliveDownCount++;
          }

          if (aliveExpiration) {
            onAliveTimerExpiration(currentId);
          }

        }
        if (!alarmActive && aliveDownCount > WARNING_THRESHOLD) {
          alarmActive = true;
          SMS_LOGGER.warn("Over " + WARNING_THRESHOLD + " DAQ/Equipment are currently down.");
        } else if (alarmActive && warningSwitchOffCountDown.decrementAndGet() == 0) {
          SMS_LOGGER.warn("DAQ/Equipment status back to normal (" + aliveDownCount + " detected as down)");
          alarmActive = false;
          warningSwitchOffCountDown = new AtomicInteger(SWITCH_OFF_COUNTDOWN);
        }
      } catch (CacheElementNotFoundException cacheEx) {
        LOGGER.warn("Failed to locate alive timer in cache on expiration check (may happen exceptionally if just removed).", cacheEx);
      } catch (Exception e) {
        LOGGER.error("Unexpected exception when checking the alive timers", e);
      }
      lastCheck = System.currentTimeMillis();
//      clusterCache.put(LAST_ALIVE_TIMER_CHECK_LONG, lastCheck);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("run() : finished checking alive timers ... ");
      }
    }
  }

  /**
   * Notifies the supervision manager.
   *
   * @param aliveTimerId the alive timer that has expired
   */
  private void onAliveTimerExpiration(final Long aliveTimerId) {
    supervisionManager.onAliveTimerExpiration(aliveTimerId);
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
