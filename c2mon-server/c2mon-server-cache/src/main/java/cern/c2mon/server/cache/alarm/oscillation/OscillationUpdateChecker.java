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
package cern.c2mon.server.cache.alarm.oscillation;

import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AliveTimerFacade;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCacheUpdater;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.alarm.AlarmQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Timer that regularly checks all the active alive timers monitoring the
 * connections to the DAQs, Equipment and SubEquipment.
 *
 * <p>
 * Notice that an alive timer is considered expired when alive-interval +
 * alive-interval/3 milliseconds have expired since the last alive message
 * arrived, where alive-interval is specific to the AliveTimer object (see
 * <code>hasExpired</code> in {@link AliveTimerFacade}).
 */
@Service
@Slf4j
public class OscillationUpdateChecker extends TimerTask implements SmartLifecycle {

  /**
   * Cluster cache key to ensure that a server does not try to access
   * LAST_CHECK_INITIALISATION_KEY during instantiation if it already exists.
   */
  private static final String LAST_CHECK_INITIALISATION_KEY = OscillationUpdateChecker.class.getName() + ".lastCheckInitialisationKey";

  /**
   * Cluster cache key for retrieving the time of last check of the alarm oscillations.
   * Across server cluster it assures that the alive check only takes place on a
   * single server.
   */
  protected static final String LAST_CHECK_LONG = OscillationUpdateChecker.class.getName() + ".lastAliveTimerCheck";

  /**
   * How often the timer checks whether the oscillation timer have expired.
   */
  protected static final long SCAN_INTERVAL = 60000L;

  /**
   * The time the server waits before doing first checks at start up (this gives
   * time for incoming alarms to be processed).
   */
  private static final long INITIAL_SCAN_DELAY = 120000L;

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  private Timer timer;

  private final AlarmCache alarmCache;

  private final OscillationUpdater oscillationUpdater;

  /** Reference to the clusterCache to share values across the cluster nodes */
  private final ClusterCache clusterCache;

  private final AlarmCacheUpdater alarmCacheUpdater;

  private final TagFacadeGateway tagFacadeGateway;

  protected final AlarmQuery alarmCacheQuery = AlarmQuery.builder().oscillating(true).build();

  /**
   * Constructor.
   *
   * @param alarmCache
   *          the alarm cache to retrieve and update alarm cache objects.
   * @param dataTagCache
   *          the data tag cache to retrieve data tag objects and check their original values.
   * @param clusterCache
   *          the cluster cache to synchronize checks.
   * @param oscillationUpdater
   *          the instance that check oscillation statuses.
   * @param AlarmCacheUpdater
   *          the alarm cache updater.
   */
  @Autowired
  public OscillationUpdateChecker(final AlarmCache alarmCache, final ClusterCache clusterCache, final OscillationUpdater oscillationUpdater, final AlarmCacheUpdater alarmCacheUpdater, final TagFacadeGateway tagFacadeGateway) {
    super();
    this.alarmCache = alarmCache;
    this.clusterCache = clusterCache;
    this.oscillationUpdater = oscillationUpdater;
    this.alarmCacheUpdater = alarmCacheUpdater;
    this.tagFacadeGateway = tagFacadeGateway;
  }


  /**
   * Initializes the clustered values
   */
  @PostConstruct
  public void init() {
    log.trace("Initialising Alarm oscillation checker ...");
    clusterCache.acquireWriteLockOnKey(LAST_CHECK_INITIALISATION_KEY);
    try {
      if (!clusterCache.hasKey(LAST_CHECK_INITIALISATION_KEY)) {
        clusterCache.put(LAST_CHECK_INITIALISATION_KEY, true);
        clusterCache.put(LAST_CHECK_LONG, Long.valueOf(0L));
      }
    } finally {
      clusterCache.releaseWriteLockOnKey(LAST_CHECK_INITIALISATION_KEY);
    }
    log.trace("Initialisation complete.");
  }

  /**
   * Starts the timer. Alive timers will be checked from then on.
   */
  @Override
  public synchronized void start() {
    log.info("Starting the C2MON Alarm oscillation timer mechanism.");
    timer = new Timer("AlarmOscillationChecker");
    timer.schedule(this, INITIAL_SCAN_DELAY, SCAN_INTERVAL);
    running = true;
  }

  /**
   * Stops the timer mechanism. No more checks are made on the alive timers.
   *
   * <p>
   * Can be restarted using the start method.
   */
  @Override
  public synchronized void stop() {
    log.info("Stopping the C2MON Alarm oscillation timer mechanism.");
    timer.cancel();
    running = false;
  }

  /**
   * Run method of the AliveTimerManager thread.
   */
  @Override
  public void run() {
    clusterCache.acquireWriteLockOnKey(LAST_CHECK_LONG);
    try {
      Long lastCheck = (Long) clusterCache.getCopy(LAST_CHECK_LONG);
      if (System.currentTimeMillis() - lastCheck.longValue() < SCAN_INTERVAL - 500L) {
        log.debug("Skipping alarm oscillation check as already performed.");
      } else {
        log.debug("checking alarm oscillation timers ... ");
        try {
          Collection<Long> oscillatingAlarmIds = alarmCache.findAlarm(alarmCacheQuery);
          if (oscillatingAlarmIds.isEmpty()) {
            log.debug("Currently no oscillating alarms");
          } else {
            log.info("Currently {} oscillating alarms", oscillatingAlarmIds.size());
            oscillatingAlarmIds.stream().forEach(this::updateAlarmOscillationFlag);
          }
        } catch (Exception e) {
          log.error("Unexpected exception when checking the Alarm oscillation timers", e);
        }

        lastCheck = Long.valueOf(System.currentTimeMillis());
        clusterCache.put(LAST_CHECK_LONG, lastCheck);

        log.debug("finished checking alarm oscillation timers");
      } // end of else block
    } finally {
      clusterCache.releaseWriteLockOnKey(LAST_CHECK_LONG);
    }
  }

  private void updateAlarmOscillationFlag(Long alarmId) {
    try {
      log.trace("Checking oscillation expiry for alarm #{}", alarmId);
      AlarmCacheObject alarmCopy = (AlarmCacheObject) alarmCache.getCopy(alarmId);

      if (!oscillationUpdater.checkOscillAlive(alarmCopy)) {
          log.trace(" -> ! Alarm #{} is not oscillating anymore, resetting oscillation flag", alarmId);
          Tag tag = tagFacadeGateway.getTag(alarmCopy.getDataTagId());
          if(tag != null) {
            alarmCacheUpdater.resetOscillationStatus(alarmCopy, tag);
          } else {
            log.error("Cannot locate data tag #{} for alarm #{} - unable to reset oscillation status", alarmCopy.getDataTagId(), alarmId);
          }
      } else {
          log.trace(" -> (!) Alarm #{} is still oscillating - no change", alarmId);
      }
    } catch (CacheElementNotFoundException e) {
      log.error("Failed to locate corresponding tag in cache for alarm #{}. This should never happen!", alarmId);
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
