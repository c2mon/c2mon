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
package cern.c2mon.cache.actions.oscillation;

import cern.c2mon.cache.actions.alarm.AlarmService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.tag.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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
 * <code>hasExpired</code> in {@code AliveTimerFacade}).
 *
 * @author Alexandros Papageorgiou
 * @author Brice Copy
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
  private static final String LAST_CHECK_LONG = OscillationUpdateChecker.class.getName() + ".lastAliveTimerCheck";

  /**
   * How often the timer checks whether the oscillation timer have expired.
   */
  private static final long SCAN_INTERVAL = 60000L;

  /**
   * The time the server waits before doing first checks at start up (this gives
   * time for incoming alarms to be processed).
   */
  private static final long INITIAL_SCAN_DELAY = 120000L;
  private final C2monCache<Alarm> alarmCacheRef;
  private final OscillationService oscillationCheckService;
  private final OscillationUpdater oscillationUpdater;
  private final AlarmService alarmService;
  private final C2monCache<DataTag> dataTagCacheRef;
  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;
  private Timer timer;

  /**
   * Constructor.
   *
   * @param alarmCacheRef           the alarm cache to retrieve and update alarm cache objects.
   * @param oscillationCheckService the component that manages the oscillation check timer
   * @param oscillationUpdater      the instance that check oscillation statuses.
   * @param alarmService
   * @param dataTagCacheRef         the data tag cache to retrieve data tag objects and check their original values.
   */
  @Inject
  public OscillationUpdateChecker(final C2monCache<Alarm> alarmCacheRef, final OscillationService oscillationCheckService,
                                  final OscillationUpdater oscillationUpdater, final AlarmService alarmService,
                                  final C2monCache<DataTag> dataTagCacheRef) {
    super();
    this.alarmCacheRef = alarmCacheRef;
    this.oscillationCheckService = oscillationCheckService;
    this.oscillationUpdater = oscillationUpdater;
    this.alarmService = alarmService;
    this.dataTagCacheRef = dataTagCacheRef;
  }


  /**
   * Initializes the clustered values
   */
  @EventListener
  public void init(ContextRefreshedEvent event) {
    log.trace("Initialising Alarm oscillation checker ...");
    oscillationCheckService.setLastOscillationCheck(0);
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
    alarmCacheRef.executeTransaction(() -> {
      long lastCheck = oscillationCheckService.getLastOscillationCheck();
      if (System.currentTimeMillis() - lastCheck < SCAN_INTERVAL - 500L) {
        log.debug("Skipping alarm oscillation check as already performed.");
      } else {
        log.debug("checking alarm oscillation timers ... ");
        try {
          Collection<Alarm> oscillatingAlarms = alarmCacheRef.query(Alarm::isOscillating);

          if (oscillatingAlarms.isEmpty()) {
            log.debug("Currently no oscillating alarms");
          } else {
            log.warn("Currently {} oscillating alarms", oscillatingAlarms.size());
            oscillatingAlarms.forEach(this::updateAlarmOscillationFlag);
          }
        } catch (Exception e) {
          log.error("Unexpected exception when checking the Alarm oscillation timers", e);
        }

        oscillationCheckService.setLastOscillationCheck(System.currentTimeMillis());

        log.debug("finished checking alarm oscillation timers");
      } // end of else block
    });
  }

  private void updateAlarmOscillationFlag(Alarm alarm) {
    AlarmCacheObject alarmCacheObject = (AlarmCacheObject) alarm;
    long alarmId = alarmCacheObject.getId();
    try {
      log.trace("Checking oscillation expiry for alarm #{}", alarmId);
      if (log.isTraceEnabled()) {
        log.trace(" -> Alarm oscillation details osc {} first osc {} count {} al ts {}", alarmCacheObject.isOscillating(),
          new Date(alarmCacheObject.getFirstOscTS()).toString(), alarmCacheObject.getCounterFault(),
          alarmCacheObject.getTriggerTimestamp().toString());
      }
      if (!oscillationUpdater.checkOscillAlive(alarmCacheObject)) {
        log.trace(" -> ! Alarm #{} is not oscillating anymore, resetting oscillation counter", alarmId);
        oscillationUpdater.resetOscillationCounter(alarmCacheObject);
        Tag tag = dataTagCacheRef.get(alarmCacheObject.getDataTagId());
        if (tag != null) {
          alarmService.stopOscillatingAndUpdate(alarmCacheObject, tag);
        } else {
          log.error("Cannot locate data tag #{} - unable to reset oscillation status", alarmCacheObject.getDataTagId());
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
