/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.configuration.handler.AlarmConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.AlarmConfigTransacted;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * See interface documentation.
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
@Service
public class AlarmConfigHandlerImpl implements AlarmConfigHandler {

  @Autowired
  private AlarmConfigTransacted alarmConfigTransacted;

  private final AlarmCache alarmCache;

  private final AlarmFacade alarmFacade;

  @Autowired
  public AlarmConfigHandlerImpl(AlarmCache alarmCache,
                                AlarmFacade alarmFacade) {
    super();
    this.alarmCache = alarmCache;
    this.alarmFacade = alarmFacade;
  }

  /**
   * Removes the alarm from the system (including datatag reference to it).
   *
   * <p>In more detail, removes the reference to the alarm in the associated
   * tag, removes the alarm from the DB and removes the alarm form the cache,
   * in that order.
   *
   * @param alarmId the id of the alarm to remove
   * @param alarmReport the configuration report for the alarm removal
   */
  @Override
  public void removeAlarm(final Long alarmId, final ConfigurationElementReport alarmReport) {
    try {
      AlarmCacheObject alarm = (AlarmCacheObject) alarmCache.getCopy(alarmId);
      alarmConfigTransacted.doRemoveAlarm(alarmId, alarmReport);
      // will be skipped if rollback exception thrown in do method
      alarmCache.remove(alarmId);
      alarmFacade.notifyOnAlarmRemoval(alarm);
    } catch (CacheElementNotFoundException e) {
      alarmReport.setWarning("Alarm " + alarmId + " is not know by the system ==> Nothing to be removed from the Alarm cache.");
    }
  }

  @Override
  public void createAlarm(ConfigurationElement element) throws IllegalAccessException {
    alarmConfigTransacted.doCreateAlarm(element);
    alarmFacade.evaluateAlarm(element.getEntityId());
  }

  @Override
  public void updateAlarm(Long alarmId, Properties properties) {
    try {
      alarmConfigTransacted.doUpdateAlarm(alarmId, properties);
      alarmFacade.evaluateAlarm(alarmId);
    } catch (UnexpectedRollbackException e) {
      log.error("Rolling back Alarm update in cache");
      alarmCache.remove(alarmId);
      alarmCache.loadFromDb(alarmId);
      throw e;
    }
  }
}
