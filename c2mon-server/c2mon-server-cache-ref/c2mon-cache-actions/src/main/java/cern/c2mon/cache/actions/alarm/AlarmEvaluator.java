package cern.c2mon.cache.actions.alarm;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class AlarmEvaluator {

  private AlarmEvaluator() {

  }

  static boolean isReadyForEvaluation(Tag tag) {
    return (
      (tag != null)
        && (tag.getValue() != null)
        && (tag.isValid())
        && (tag.getDataTagQuality().isInitialised())
        && (tag.getTimestamp() != null)
    );
  }

  static String evaluateAdditionalInfo(final Alarm alarm, final Tag tag) {
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
   * Detects if an alarm has changed based on the tag contents.
   * <p>
   * Absolutely no side effects (and please don't introduce any)
   *
   * @param alarm
   * @param tag
   * @return {@link Boolean#TRUE} if the has changes based on the tag
   */
  static boolean detectChanges(final Alarm alarm, final Tag tag) {
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
}
