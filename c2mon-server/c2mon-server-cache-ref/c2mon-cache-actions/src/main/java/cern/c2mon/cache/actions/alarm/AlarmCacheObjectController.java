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
package cern.c2mon.cache.actions.alarm;

import cern.c2mon.cache.actions.tag.CommonTagOperations;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * Contains the routing logic for the alarm cache update. The alarm cache listeners will get informed depending, if it
 * is an oscillating alarm or not.
 *
 * @author Mark Brightwell, Emiliano Piselli, Brice Copy, Matthias Braeger, Alexandros Papageorgiou
 */
@Service
@Slf4j
public final class AlarmCacheObjectController {

  public static String evaluateAdditionalInfo(final Alarm alarm, final Tag tag) {
    String additionalInfo = "";

    if (tag != null) {
      switch (tag.getMode()) {
        case DataTagConstants.MODE_MAINTENANCE:
          additionalInfo = tag.isValid() ? "[M]" : "[M][?]";
          break;
        case DataTagConstants.MODE_TEST:
          additionalInfo = tag.isValid() ? "[T]" : "[T][?]";
          break;
        default:
          additionalInfo = tag.isValid() ? "" : "[?]";
      }
    }

    if (alarm != null && alarm.isOscillating()) {
      additionalInfo = additionalInfo + Alarm.ALARM_INFO_OSC;
    }

    if (tag != null) {
      // Add another flag to the info if the value is simulated
      if (tag.isSimulated()) {
        additionalInfo = additionalInfo + "[SIM]";
      }
    }
    return additionalInfo;
  }

  /**
   * Computes the alarm state corresponding to the new tag value
   *
   * @param alarmCacheObject The current alarm object in the cache
   * @param tag   The tag update
   * @return {@link Boolean#TRUE} is the Alarm was changed, false otherwise
   */
  public static boolean updateAlarmBasedOnTag(final AlarmCacheObject alarmCacheObject, final Tag tag) {
    // TODO Is there any case where we want to evaluate despite invalid tag?
    // TODO If so, readd the tag valid check to updateAlarmState
    if (!CommonTagOperations.isReadyForEvaluation(tag)) {
      log.debug("Alarm update called with erroneous Tag - leaving alarm status unchanged for alarm #{}", alarmCacheObject.getId());
      return false;
    }

    if (!detectChanges(alarmCacheObject, tag)) {
      log.debug("Alarm update called but no changes were detected based on the tag - leaving alarm status unchanged for alarm #{}", alarmCacheObject.getId());
      return false;
    }

    // If those passed, let's mutate this object
    updateInfo(alarmCacheObject, tag);

    // TODO Is there any case where we don't want to update timestamps?
    updateTimestamps(alarmCacheObject, tag);

    updateAlarmState(alarmCacheObject, tag);

    return true;
  }

  /**
   * No side effects, True/False response
   *
   * @param alarm
   * @param tag
   * @return {@link Boolean#TRUE} if the has changes based on the tag
   */
  private static boolean detectChanges(final Alarm alarm, final Tag tag) {
    AlarmCacheObject alarmCacheObject = (AlarmCacheObject) alarm;

    // If the alarm has not been initialized, we need to update it
    if (isAlarmUninitialised(alarmCacheObject))
      return true;

    // If the new or previous state is active, we need to update
    // TODO Oscillation check here?
    boolean newState = alarmCacheObject.getCondition().evaluateState(tag.getValue());
    if (newState || alarmCacheObject.isActive()) {
      return true;
    }

    // If the internal state has changed, we should try to update
    // Oscillation checks might mean that in the end this update could be dropped though
    boolean internalStateHasChanged = alarmCacheObject.isInternalActive() != newState;
    if ((tag.isValid() && internalStateHasChanged)) {
      return true;
    }

    // Human readable version, will be optimized by the compiler
    String currentInfo = alarmCacheObject.getInfo();
    String evaluatedInfo = evaluateAdditionalInfo(alarmCacheObject, tag);
    return !evaluatedInfo.equals(currentInfo);
  }

  private static boolean isAlarmUninitialised(final AlarmCacheObject alarmCacheObject) {
    return alarmCacheObject.getTriggerTimestamp() == null || alarmCacheObject.getTriggerTimestamp().getTime() == 0L;
  }

  private static void updateInfo(AlarmCacheObject alarmCacheObject, Tag tag) {
    alarmCacheObject.setInfo(evaluateAdditionalInfo(alarmCacheObject, tag));
  }

  private static void updateTimestamps(final AlarmCacheObject alarmCacheObject, final Tag tag) {
    alarmCacheObject.setTriggerTimestamp(new Timestamp(System.currentTimeMillis()));
    alarmCacheObject.setSourceTimestamp(tag.getTimestamp());
  }

  private static void updateAlarmState(final AlarmCacheObject alarmCacheObject, Tag tag) {
    boolean newState = alarmCacheObject.getCondition().evaluateState(tag.getValue());

    alarmCacheObject.setInternalActive(newState);

    if (alarmCacheObject.isOscillating()) {
      // When oscillating we force the alarm to *active*
      // (only the *internalActive* property reflects the true status)
      alarmCacheObject.setActive(true);
    } else {
      alarmCacheObject.setActive(newState);
    }
    log.trace("Alarm #{} changed STATE to {}", alarmCacheObject.getId(), alarmCacheObject.isActive());
  }

}
