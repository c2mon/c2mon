/******************************************************************************
 * Copyright (C) 2010-2018 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.alarm.impl;

import java.sql.Timestamp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCacheUpdater;
import cern.c2mon.server.common.alarm.AlarmCondition;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagConstants;

@Component
@Slf4j
public final class AlarmCacheUpdaterImpl implements AlarmCacheUpdater {

  @Autowired
  AlarmCache alarmCache;

  /**
   * Logic kept the same as in TIM1 (see {@link AlarmFacade}). The locking of
   * the objets is done in the public class. Notice, in this case the update()
   * method is putting the changes back into the cache.
   */
  @Override
  public Alarm update(final Alarm alarm, final Tag tag) {
    AlarmCacheObject alarmCacheObject = (AlarmCacheObject) alarm;
    // this time is then used in LASER publication as user timestamp
    Timestamp alarmTime = tag.getCacheTimestamp();
    // not possible to evaluate alarms with associated null tag; occurs during
    // normal operation
    // (may change in future is alarm state depends on quality f.eg.)
    if (tag.getValue() == null) {
      log.debug("Alarm update called with null Tag value - leaving Alarm status unchanged at " + alarm.getState());

      // change the alarm timestamp, if the alarm has never been initialised
      if (alarmCacheObject.getTimestamp().equals(new Timestamp(0))) {
        alarmCacheObject.setTimestamp(alarmTime);
      }
      return alarmCacheObject;
    }

    if (!tag.getDataTagQuality().isInitialised()) {
      log.debug("Alarm update called with uninitialised Tag - leaving Alarm status unchanged.");
      return alarm;
    }

    // timestamp should never be null
    if (tag.getTimestamp() == null) {
      log.warn("update() : tag value or timestamp null -> no update");
      throw new IllegalArgumentException("update method called on Alarm facade with either null tag value or null tag timestamp.");
    }

    // Compute the alarm state corresponding to the new tag value
    String newState = alarmCacheObject.getCondition().evaluateState(tag.getValue());




    // Return immediately if the alarm new state is null
    if (newState == null) {
      log.error("update() : new state would be NULL -> no update.");
      throw new IllegalStateException("Alarm evaluated to null state!");
    }

    // Return if new state is TERMINATE and old state was also TERMINATE, return
    // original alarm (no need to save in cache)
    if (newState.equals(AlarmCondition.TERMINATE) && alarmCacheObject.getState().equals(AlarmCondition.TERMINATE)) {
      return alarm;
    }

    // Build up a prefix according to the tag value's validity and mode
    String additionalInfo = null;

    switch (tag.getMode()) {
    case DataTagConstants.MODE_MAINTENANCE:
      if (tag.isValid()) {
        additionalInfo = "[M]";
      } else {
        additionalInfo = "[M][?]";
      }
      break;
    case DataTagConstants.MODE_TEST:
      if (tag.isValid()) {
        additionalInfo = "[T]";
      } else {
        additionalInfo = "[T][?]";
      }
      break;
    default:
      if (tag.isValid()) {
        additionalInfo = "";
      } else {
        additionalInfo = "[?]";
      }
    }

    if (alarm.isOscillating()) {
      additionalInfo = additionalInfo + "[OSC]";
    }

    // Add another flag to the info if the value is simulated
    if (tag.isSimulated()) {
      additionalInfo = additionalInfo + "[SIM]";
    }

    // Default case: change the alarm's state
    // (1) if the alarm has never been initialised
    // (2) if tag is VALID and the alarm changes from ACTIVE->TERMINATE or
    // TERMIATE->ACTIVE
    if (alarmCacheObject.getTimestamp().equals(new Timestamp(0)) || (tag.isValid() && !alarmCacheObject.getState().equals(newState))) {

      log.trace(new StringBuffer("update(): alarm ").append(alarmCacheObject.getId()).append(" changed STATE to ").append(newState).toString());

      alarmCacheObject.setState(newState);
      alarmCacheObject.setTimestamp(alarmTime);
      alarmCacheObject.setInfo(additionalInfo);
      alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
      return alarmCacheObject;
    }

    // Even if the alarm state itself hasn't change, the additional
    // information
    // related to the alarm (e.g. whether the alarm is valid or invalid)
    // might
    // have changed
    if (alarmCacheObject.getInfo() == null) {
      alarmCacheObject.setInfo("");
    }
    if (!alarmCacheObject.getInfo().equals(additionalInfo)) {
      log.trace(new StringBuffer("update(): alarm ").append(alarmCacheObject.getId()).append(" changed INFO to ").append(additionalInfo).toString());

      alarmCacheObject.setInfo(additionalInfo);
      alarmCacheObject.setTimestamp(alarmTime);
      if (!alarmCacheObject.getState().equals(AlarmCondition.TERMINATE)) {
        alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
      }
      return alarmCacheObject;
    }

    // In all other cases, the value of the alarm related to the DataTag has
    // not changed. No need to publish an alarm change.
    log.trace(new StringBuffer("update(): alarm ").append(alarmCacheObject.getId()).append(" has not changed.").toString());

    // no change so no listener notification in this case

    // this.alarmChange = CHANGE_NONE;
    return alarmCacheObject;
  }

}
