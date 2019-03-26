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
import cern.c2mon.server.cache.alarm.config.OscillationProperties;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCacheUpdater;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.alarm.AlarmQuery;

/**
 * Timer that regularly checks all the active alive timers monitoring the
 * connections to the DAQs, Equipment and SubEquipment.
 *
 * <p>
 * Notice that an alive timer is considered expired when alive-interval +
 * alive-interval/3 milliseconds have expired since the last alive message
 * arrived, where alive-interval is specific to the AliveTimer object (see
 * <code>hasExpired</code> in {@link AliveTimerFacade}).
 *
 * @author Mark Brightwell
 *
 */
@Service
@Slf4j
public class OscillationUpdateChecker extends TimerTask implements SmartLifecycle {

  /**
   * Cluster cache key to ensure that a server does not try to access
   * LAST_ALIVE_TIMER_CHECK_LONG during instantiation if it already exists. This
   * is because a cache loading blockage can happen if a server holds
   * LAST_ALIVE_TIMER_CHECK_LONG while another server starts up. See
   * https://issues.cern.ch/browse/TIMS-1037.
   */
  private static final String LAST_CHECK_INITIALISATION_KEY = OscillationUpdateChecker.class.getName() + ".lastCheckInitialisationKey";

  /**
   * Cluster cache key for retrieving the time of last check of the alives.
   * Across server cluster it assures that the alive check only takes place on a
   * single server.
   */
  private static final String LAST_CHECK_LONG = OscillationUpdateChecker.class.getName() + ".lastAliveTimerCheck";

  /**
   * How often the timer checks whether the alive timer have expired: 10 seconds (in millis)
   */
  private static final int SCAN_INTERVAL = 10000;

  /**
   * The time the server waits before doing first checks at start up (this gives
   * time for incoming alives to be processed).
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
   * Reference to alive timer cache.
   */
  private AlarmCache alarmCache;

  private final TagFacadeGateway tagFacade;

  /**
   * Reference to alive oscillationUpdater.
   */
  private OscillationUpdater oscillationUpdater;

  /** Reference to the clusterCache to share values across the cluster nodes */
  private final ClusterCache clusterCache;

  private final AlarmCacheUpdater alarmCacheUpdater;

  /**
   * Constructor.
   *
   * @param alarmCache
   *          the alarm cache
   * @param clusterCache
   *          Reference to the clusterCache to share values accross teh cluster
   *          nodes
   */
  @Autowired
  public OscillationUpdateChecker(final AlarmCache alarmCache, final TagFacadeGateway tagFacade, final ClusterCache clusterCache, final OscillationUpdater oscillationUpdater, final AlarmCacheUpdater alarmCacheUpdater) {
    super();
    this.alarmCache = alarmCache;
    this.tagFacade = tagFacade;
    this.clusterCache = clusterCache;
    this.oscillationUpdater = oscillationUpdater;
    this.alarmCacheUpdater = alarmCacheUpdater;
  }

  @Autowired
  OscillationProperties oscillationProperties;

  /**
   * Initialises the clustered values
   */
  @PostConstruct
  public void init() {
    log.trace("Initialising AliveTimerChecker...");
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
    log.info("Starting the C2MON alive timer mechanism.");
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
    log.info("Stopping the C2MON alive timer mechanism.");
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
      if (System.currentTimeMillis() - lastCheck.longValue() < 9000) {
        log.debug("Skipping alarm oscillation check as already performed.");
      } else {
        log.debug("checking alarm oscillation timers ... ");
        try {
          AlarmQuery query = AlarmQuery.builder().oscillating(true).build();
          Collection<Long> oscillatingAlarmIds = alarmCache.findAlarm(query);
          oscillatingAlarmIds.stream().forEach(this::checkOscillation);
        } catch (Exception e) {
          log.error("Unexpected exception when checking the active oscillating alarms", e);
        }

        lastCheck = Long.valueOf(System.currentTimeMillis());
        clusterCache.put(LAST_CHECK_LONG, lastCheck);

        if (log.isDebugEnabled()) {
          log.debug("run() : finished checking alarm oscillation timers ... ");
        }
      } // end of else block
    } finally {
      clusterCache.releaseWriteLockOnKey(LAST_CHECK_LONG);
    }
  }

  private void checkOscillation(Long alarmId) {
    try {
      AlarmCacheObject alarmCopy = (AlarmCacheObject) alarmCache.getCopy(alarmId);
      Tag tag = tagFacade.getTag(alarmCopy.getDataTagId());
      if (!oscillationUpdater.checkOscillAlive(alarmCopy)) {
        oscillationUpdater.resetOscillationCounter(alarmCopy);
        alarmCopy.setOscillating(false);
        alarmCacheUpdater.update(alarmCopy, tag);
        alarmCopy.setInfo(AlarmCacheUpdater.evaluateAdditionalInfo(alarmCopy, tag));
        alarmCache.put(alarmCopy.getId(), alarmCopy);
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
