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
        alarm.getId(), alarm.getTagId(),tag.getId());
      return false;
    }

    if (isTagMissingCriticalInformation(tag)) {
      log.debug("Erroneous Tag information - alarm #{} will not be updated", alarm.getId());
      return false;
    }

    if (!isAlarmChangedBasedOnTag(alarm, tag)) {
      log.debug("No changes were detected based on the tag - will not update alarm #{}", alarm.getId());
      return false;
    }

    return true;
  }

  private static boolean isAlarmConnectedToTag(Alarm alarm, Tag tag) {
    return Objects.equals(alarm.getTagId(), tag.getId());
  }

  private static boolean isTagMissingCriticalInformation(Tag tag) {
    boolean tagExistWithValue = tag == null || tag.getValue() == null;
    boolean tagIsInvalid = !tag.isValid() || !tag.getDataTagQuality().isInitialised();

    return tagExistWithValue
            || tagIsInvalid
            || tag.getTimestamp() == null;
  }

  static String createAdditionalInfoString(final Alarm alarm, final Tag tag) {
    StringBuilder additionalInfo = new StringBuilder();

    if (tag != null) {
      switch (tag.getMode()) {
        case DataTagConstants.MODE_MAINTENANCE:
          additionalInfo.append(tag.isValid() ? "[M]" : "[M][?]");
          break;
        case DataTagConstants.MODE_TEST:
          additionalInfo.append(tag.isValid() ? "[T]" : "[T][?]");
          break;
        default:
          additionalInfo.append(tag.isValid() ? "" : "[?]");
      }
    }

    if (alarm != null && alarm.isOscillating()) {
      additionalInfo.append(additionalInfo + Alarm.ALARM_INFO_OSC);
    }

    // Add another flag to the info if the value is simulated
    if (tag != null && tag.isSimulated()) {
        additionalInfo.append(additionalInfo + "[SIM]");
    }

    return additionalInfo.toString();
  }

  private static boolean isAlarmChangedBasedOnTag(final Alarm alarm, final Tag tag) {
    AlarmCacheObject alarmCacheObject = (AlarmCacheObject) alarm;

    // If the alarm has not been initialized, we need to update it
    if (isAlarmUninitialised(alarmCacheObject)) {
      return true;
    }

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
