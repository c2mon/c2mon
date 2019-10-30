package cern.c2mon.server.common.alarm;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagConstants;

public interface AlarmCacheObjectController {

  /**
   * Computes the alarm state corresponding to the new tag value and updates the Alarm cache, if required.
   * @param alarm The current alarm object in the cache
   * @param tag The tag update
   * @return The updated alarm object
   */
  Alarm update(final Alarm alarm, final Tag tag);

  /**
   * Resets the oscillation flag to false and computes the alarm state corresponding to the actual tag value.
   * It will also updates the Alarm cache and notify the listeners.
   * @param alarm The current alarm object in the cache
   * @param tag The tag update
   * @return The updated alarm object
   */
  void resetOscillationStatus(final AlarmCacheObject alarm, final Tag tag);

  static String evaluateAdditionalInfo(final Alarm alarm, final Tag tag){
    String additionalInfo = "";

    if (tag != null) {
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
}
