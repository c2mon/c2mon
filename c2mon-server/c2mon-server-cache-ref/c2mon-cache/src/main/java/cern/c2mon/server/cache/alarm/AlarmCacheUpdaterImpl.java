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
package cern.c2mon.server.cache.alarm;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.oscillation.OscillationUpdater;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCacheUpdater;
import cern.c2mon.server.common.tag.Tag;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * Contains the routing logic for the alarm cache update. The alarm cache listeners will get informed depending, if it
 * is an oscillating alarm or not.
 *
 * @author Mark Brightwell, Emiliano Piselli, Brice Copy, Matthias Braeger
 */
@Service
@Slf4j
public final class AlarmCacheUpdaterImpl implements AlarmCacheUpdater {

  @Autowired
  @Setter(AccessLevel.PROTECTED)
  private C2monCache<Alarm> alarmCache;

  @Autowired
  @Setter(AccessLevel.PROTECTED)
  OscillationUpdater oscillationUpdater;

  /**
   * Logic kept the same as in TIM1 (see {@link AlarmFacade}). The locking of
   * the cache object is done in the public class. Notice, in this case the update()
   * method is putting the changes back into the cache.
   */
  @Override
  public Alarm update(final Alarm alarm, final Tag tag) {
    AlarmCacheObject alarmCacheObject = (AlarmCacheObject) alarm;

    // not possible to evaluate alarms with associated null tag; occurs during
    // normal operation
    // (may change in future is alarm state depends on quality f.eg.)
    if (tag.getValue() == null) {
      log.debug("Alarm update called with null Tag value - leaving alarm status unchanged at {} for #{}", alarm.isActive(), alarm.getId());

      // change the alarm timestamp, if the alarm has never been initialised
      if (isAlarmUninitialised(alarmCacheObject)) {
        changeInfoField(alarmCacheObject, tag);
        changeTimestamps(alarmCacheObject, tag);
      }
      return alarmCacheObject;
    }

    if (!tag.getDataTagQuality().isInitialised()) {
      log.debug("Alarm update called with uninitialised Tag - leaving slarm status unchanged for alarm #{}", alarm.getId());
      return alarm;
    }

    // timestamp should never be null
    if (tag.getTimestamp() == null) {
      log.warn("Tag value or timestamp null -> no update on alarm #{}", alarm.getId());
      throw new IllegalArgumentException("update method called on Alarm facade with either null tag value or null tag timestamp");
    }

    return updateAlarmCacheObject(alarmCacheObject, tag, false);
  }

  @Override
  public void resetOscillationStatus(final AlarmCacheObject alarmCopy, final Tag tag) {
    alarmCopy.setOscillating(false);
    updateAlarmCacheObject(alarmCopy, tag, true);
  }

  /**
   * Contains all the logical steps to:
   * <li> compute the new alarm state
   * <li> changes the oscillation status
   * <li> update info field and timestamps
   *
   * @param alarmCacheObject The current alarm cache object
   * @param tag the tag update
   * @param resetOscillationStatus true, if method is triggered byu the Oscillation updater task
   * @return The updated alarm cache object
   */
  private AlarmCacheObject updateAlarmCacheObject(final AlarmCacheObject alarmCacheObject, final Tag tag, boolean resetOscillationStatus) {
    // Compute the alarm state corresponding to the new tag value
    boolean newState = alarmCacheObject.getCondition().evaluateState(tag.getValue());

    // Return if new state is TERMINATE and old state was also TERMINATE, return
    // original alarm (no need to save in cache)
    if ( !resetOscillationStatus && !newState && !alarmCacheObject.isActive() ) {
      return alarmCacheObject;
    }

    boolean alarmStateHasChanged = alarmCacheObject.isInternalActive() != newState;

    // We only allow activating the alarm if the tag is valid.
    if(tag.isValid()){
      alarmCacheObject.setActive(newState);
      alarmCacheObject.setInternalActive(newState);
    }

    // Default case: change the alarm's state
    // (1) if the alarm has never been initialised OR
    // (2) if tag is VALID and the alarm changes from ACTIVE->TERMINATE or TERMINATE->ACTIVE
    if (isAlarmUninitialised(alarmCacheObject) || (tag.isValid() && alarmStateHasChanged) ) {
      return commitAlarmStateChange(alarmCacheObject, tag);
    }

    // Check if INFO field has changed and alarm is active
    String oldAlarmInfo = alarmCacheObject.getInfo();
    changeInfoField(alarmCacheObject, tag);
    if (!alarmCacheObject.getInfo().equals(oldAlarmInfo) && (alarmCacheObject.isActive() || resetOscillationStatus)) {
      log.trace("Alarm #{} changed INFO to {}", alarmCacheObject.getId(), alarmCacheObject.getInfo());
      changeTimestamps(alarmCacheObject, tag);
      alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
      return alarmCacheObject;
    }

    // In all other cases, the value of the alarm related to the DataTag has
    // not changed. No need to publish an alarm change.
    log.trace("Alarm #{} has not changed", alarmCacheObject.getId());

    return alarmCacheObject;
  }

  boolean isAlarmUninitialised(final AlarmCacheObject alarmCacheObject) {
    return alarmCacheObject.getTriggerTimestamp() == null || alarmCacheObject.getTriggerTimestamp().getTime() == 0L;
  }

  private void changeTimestamps(final AlarmCacheObject alarmCacheObject, final Tag tag) {
    alarmCacheObject.setTriggerTimestamp(new Timestamp(System.currentTimeMillis()));
    alarmCacheObject.setSourceTimestamp(tag.getTimestamp());
  }

  /**
   * Build up a prefix according to the tag value's validity and mode
   */
  private void changeInfoField(final AlarmCacheObject alarmCacheObject, final Tag tag) {
    alarmCacheObject.setInfo(AlarmCacheUpdater.evaluateAdditionalInfo(alarmCacheObject, tag));
  }

  private AlarmCacheObject commitAlarmStateChange(final AlarmCacheObject alarmCacheObject, final Tag tag) {
    log.trace("Alarm #{} changed STATE to {}", alarmCacheObject.getId(), alarmCacheObject.isActive());

    changeTimestamps(alarmCacheObject, tag);

    // Check the oscillating status
    boolean wasAlreadyOscillating = alarmCacheObject.isOscillating();
    oscillationUpdater.updateOscillationStatus(alarmCacheObject);

    changeInfoField(alarmCacheObject, tag);

    if (alarmCacheObject.isOscillating()) {
        // When oscillating we force the alarm to *active*
        // (only the *internalActive* property reflects the true status)
        alarmCacheObject.setActive(true);
    }
    if (wasAlreadyOscillating) {
        alarmCache.putQuiet(alarmCacheObject.getId(), alarmCacheObject);
    } else {
        alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
    }

    return alarmCacheObject;
  }

}
