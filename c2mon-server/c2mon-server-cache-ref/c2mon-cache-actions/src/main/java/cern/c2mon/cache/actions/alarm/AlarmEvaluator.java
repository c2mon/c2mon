package cern.c2mon.cache.actions.alarm;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Strictly business logic related to alarm
 *
 * No functions here have side effects. Please don't introduce any!
 * Write mutating code in the {@link AlarmService} instead
 */
@Slf4j
class AlarmEvaluator {

  private AlarmEvaluator() {

  }

  static boolean alarmShouldBeUpdated(Alarm alarm, Tag tag) {
    if (!isAlarmConnectedToTag(alarm, tag)) {
      log.warn("Alarm #{} with datatagId #{} will not be updated - the tag passed in the update does not have a matching id: #{}",
        alarm.getId(), alarm.getDataTagId(),tag.getId());
      return false;
    }

    if (isTagMissingCriticalInformation(tag)) {
      log.debug("Erroneous Tag information - alarm #{} will not be updated", alarm.getId());
      return false;
    }

    // TODO Oscillation check? Otherwise the active and internalActive properties on this are pretty useless
    if (!isAlarmChangedBasedOnTag(alarm, tag)) {
      log.debug("No changes were detected based on the tag - will not update alarm #{}", alarm.getId());
      return false;
    }

    return true;
  }

  private static boolean isAlarmConnectedToTag(Alarm alarm, Tag tag) {
    return Objects.equals(alarm.getDataTagId(), tag.getId());
  }

  private static boolean isTagMissingCriticalInformation(Tag tag) {
    return tag == null
      || tag.getValue() == null
      || !tag.isValid()
      || !tag.getDataTagQuality().isInitialised()
      || tag.getTimestamp() == null;
  }

  static String createAdditionalInfoString(final Alarm alarm, final Tag tag) {
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

  private static boolean isAlarmChangedBasedOnTag(final Alarm alarm, final Tag tag) {
    AlarmCacheObject alarmCacheObject = (AlarmCacheObject) alarm;

    // If the alarm has not been initialized, we need to update it
    if (isAlarmUninitialised(alarmCacheObject))
      return true;

    boolean newState = alarmCacheObject.getCondition().evaluateState(tag.getValue());
    if (newState != alarmCacheObject.isActive() || newState != alarmCacheObject.isInternalActive()) {
      return true;
    }

    // Human readable version, will be optimized by the compiler
    String currentInfo = alarmCacheObject.getInfo();
    String evaluatedInfo = createAdditionalInfoString(alarmCacheObject, tag);
    return !evaluatedInfo.equals(currentInfo);
  }

  private static boolean isAlarmUninitialised(final AlarmCacheObject alarmCacheObject) {
    return alarmCacheObject.getTriggerTimestamp() == null || alarmCacheObject.getTriggerTimestamp().getTime() == 0L;
  }
}
