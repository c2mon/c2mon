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
package cern.c2mon.server.configuration.handler.impl;

import cern.c2mon.cache.actions.alarm.AlarmService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.configuration.handler.AlarmConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.AlarmConfigTransacted;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.CacheEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import java.sql.Timestamp;
import java.util.Properties;

/**
 * See interface documentation.
 *
 * @author Mark Brightwell
 */
@Slf4j
@Service
public class AlarmConfigHandlerImpl implements AlarmConfigHandler {

  private AlarmConfigTransacted alarmConfigTransacted;

  private C2monCache<Alarm> alarmCache;

  private AlarmService alarmService;

  @Autowired
  public AlarmConfigHandlerImpl(AlarmConfigTransacted alarmConfigTransacted, AlarmService alarmService) {
    super();
    this.alarmConfigTransacted = alarmConfigTransacted;
    this.alarmCache = alarmService.getCache();
    this.alarmService = alarmService;
  }

  /**
   * Removes the alarm from the system (including datatag reference to it).
   *
   * <p>In more detail, removes the reference to the alarm in the associated
   * tag, removes the alarm from the DB and removes the alarm form the cache,
   * in that order.
   *
   * @param alarmId     the id of the alarm to remove
   * @param alarmReport the configuration report for the alarm removal
   */
  @Override
  public Void remove(final Long alarmId, final ConfigurationElementReport alarmReport) {
    try {
      AlarmCacheObject alarm = (AlarmCacheObject) alarmCache.get(alarmId);
      alarmConfigTransacted.doRemoveAlarm(alarmId, alarmReport);
      alarmCache.remove(alarmId); //will be skipped if rollback exception thrown in do method

      alarm.setActive(false);
      alarm.setInfo("Alarm was removed");
      alarm.setTriggerTimestamp(new Timestamp(System.currentTimeMillis()));

      // TODO Could this be switched to some sort of REMOVED event?
      alarmCache.getCacheListenerManager().notifyListenersOf(CacheEvent.UPDATE_ACCEPTED, alarm);
    } catch (CacheElementNotFoundException e) {
      alarmReport.setWarning("Alarm " + alarmId + " is not know by the system ==> Nothing to be removed from the Alarm cache.");
    }
    return null;
  }

  @Override
  public Void create(ConfigurationElement element) throws IllegalAccessException {
    alarmConfigTransacted.doCreateAlarm(element);
    alarmService.evaluateAlarm(element.getEntityId());
    return null;
  }

  @Override
  public Void update(Long alarmId, Properties properties) {
    try {
      alarmConfigTransacted.doUpdateAlarm(alarmId, properties);
      alarmService.evaluateAlarm(alarmId);
    } catch (UnexpectedRollbackException e) {
      log.error("Rolling back Alarm update in cache");
      alarmCache.remove(alarmId);
      alarmCache.loadFromDb(alarmId);
      throw e;
    }
    return null;
  }
}
