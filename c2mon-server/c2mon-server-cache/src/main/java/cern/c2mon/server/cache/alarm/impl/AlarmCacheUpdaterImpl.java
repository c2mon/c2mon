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
package cern.c2mon.server.cache.alarm.impl;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.cache.alarm.oscillation.OscillationUpdater;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCacheUpdater;
import cern.c2mon.server.common.tag.Tag;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public final class AlarmCacheUpdaterImpl implements AlarmCacheUpdater {

  @Autowired
  @Setter(AccessLevel.PROTECTED)
  private AlarmCache alarmCache;

  @Autowired
  @Setter(AccessLevel.PROTECTED)
  OscillationUpdater oscillationUpdater;

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
      log.debug("Alarm update called with null Tag value - leaving Alarm status unchanged at " + alarm.isActive());

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

    // Check the oscillating status
    boolean wasAlreadyOscillating = alarmCacheObject.isOscillating();
    oscillationUpdater.update(alarm, tag);

    // Compute the alarm state corresponding to the new tag value
    boolean newState = alarmCacheObject.getCondition().evaluateState(tag.getValue());

    // Return if new state is TERMINATE and old state was also TERMINATE, return
    // original alarm (no need to save in cache)
    if (newState == false && alarmCacheObject.isActive() == false) {
      return alarm;
    }

    // Build up a prefix according to the tag value's validity and mode
    String additionalInfo = AlarmCacheUpdater.evaluateAdditionalInfo(alarm, tag);

    // Default case: change the alarm's state
    // (1) if the alarm has never been initialised
    // (2) if tag is VALID and the alarm changes from ACTIVE->TERMINATE or
    // TERMINATE->ACTIVE
    if (alarmCacheObject.getTimestamp().equals(new Timestamp(0)) || (tag.isValid() && ((alarmCacheObject.isActive() != newState) || (alarm.isOscillating())))) {

      log.trace(new StringBuffer("update(): alarm ").append(alarmCacheObject.getId()).append(" changed STATE to ").append(newState).toString());
      alarmCacheObject.setTimestamp(alarmTime);
      alarmCacheObject.setInfo(additionalInfo);

      if (alarm.isOscillating()) {
        alarmCacheObject.setActive(true);
        if (wasAlreadyOscillating) {
          alarmCache.putQuiet(alarmCacheObject);
        } else {
          alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
        }
      } else {
        alarmCacheObject.setActive(newState);
        alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
      }
      return alarmCacheObject;
    }

    // Even if the alarm state itself hasn't changed, the additional
    // information
    // related to the alarm (e.g. whether the alarm is valid or invalid)
    // might have changed
    if (alarmCacheObject.getInfo() == null) {
      alarmCacheObject.setInfo("");
    }
    if (!alarmCacheObject.getInfo().equals(additionalInfo)) {
      log.trace(new StringBuffer("update(): alarm ").append(alarmCacheObject.getId()).append(" changed INFO to ").append(additionalInfo).toString());

      alarmCacheObject.setInfo(additionalInfo);
      alarmCacheObject.setTimestamp(alarmTime);
      if (alarmCacheObject.isActive() != false) {
        alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
      }
      return alarmCacheObject;
    }

    // In all other cases, the value of the alarm related to the DataTag has
    // not changed. No need to publish an alarm change.
    log.trace(new StringBuffer("update(): alarm ").append(alarmCacheObject.getId()).append(" has not changed.").toString());

    return alarmCacheObject;
  }

}
