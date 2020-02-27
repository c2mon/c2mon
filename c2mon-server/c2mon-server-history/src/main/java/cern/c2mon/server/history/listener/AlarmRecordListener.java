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
package cern.c2mon.server.history.listener;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.history.logger.BatchLogger;
import cern.c2mon.shared.common.CacheEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static cern.c2mon.server.common.util.Java9Collections.listOf;

/**
 * Listens to updates in the Alarm cache and calls the DAO
 * for logging these to the history database.
 *
 * @author Alexandros Papageorgiou, Felix Ehm
 */
@Component
public class AlarmRecordListener {

  private C2monCache<Alarm> alarmCache;

  /**
   * Bean that logs Tags into the history.
   */
  private BatchLogger<Alarm> alarmLogger;

  /**
   * Autowired constructor.
   *
   * @param alarmCache  for registering cache listeners
   * @param alarmLogger for logging cache objects to the history
   */
  @Inject
  public AlarmRecordListener(final C2monCache<Alarm> alarmCache, final BatchLogger<Alarm> alarmLogger) {
    this.alarmCache = alarmCache;
    this.alarmLogger = alarmLogger;
  }

  /**
   * Registers to be notified of all Tag updates (data, rule and control tags).
   */
  @PostConstruct
  public void init() {
    alarmCache.getCacheListenerManager().registerListener(alarm ->
        alarmLogger.log(listOf(alarm))
      , CacheEvent.UPDATE_ACCEPTED);
  }
}
