package cern.c2mon.cache.actions.datatag;

import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

import static cern.c2mon.shared.common.type.TypeConverter.isKnownClass;

@Slf4j
public class DataTagEvaluator {

  private DataTagEvaluator() {

  }

  public static boolean dataTypeMatches(DataTag dataTag, SourceDataTagValue sourceDataTagValue) {
    return (sourceDataTagValue.getValue() != null
      && isKnownClass(sourceDataTagValue.getDataType())
      && sourceDataTagValue.getDataType().equalsIgnoreCase(dataTag.getDataType())
    );
  }

  /**
   * Method containing all the logic for filtering out incoming datatag updates before any updates are
   * attempted.
   *
   * @return true if the update should be filtered out, false if it should be kept
   */
  public static boolean filterout(DataTag olderTag, DataTag newerTag) {

    //set the timestamps to compare:
    //(1)if both daq timestamps are set, compare these
    //(2)if not, use the source timestamps
    Timestamp olderTagTimestamp, newerTagTimestamp;

    if (olderTag.getDaqTimestamp() != null && newerTag.getDaqTimestamp() != null) {
      olderTagTimestamp = olderTag.getDaqTimestamp();
      newerTagTimestamp = newerTag.getDaqTimestamp();
    } else if (olderTag.getSourceTimestamp() != null && newerTag.getTimestamp() != null) {
      //only for backwards compatibility until all DAQs are sending DAQ timestamps
      olderTagTimestamp = olderTag.getSourceTimestamp();
      newerTagTimestamp = newerTag.getTimestamp();
    } else {
      return false; //since only server timestamp is set on olderTag, all incoming source values should be accepted
    }

    //neither timestamps should be null from here


    // Do NOT update the tag if the new timestamp is OLDER.
    // EXCEPTION:
    // If the datatag is currently marked as INACCESSIBLE, we can override the value BUT
    // the timestamp will be the current time.
    if (newerTagTimestamp.before(olderTagTimestamp)) {
      if (olderTag.getDataTagQuality() == null || olderTag.getDataTagQuality().isAccessible()) {
        log.debug("update() : older timestamp and not inaccessible -> reject update");
        return true;
      } else {
        log.debug("update() : older timestamp but tag currently inaccessible -> update with older timestamp");
      }
    }

    // If the timestamp of the new value is the same as the old timestamp, only
    // perform an update if the values are different (and valid). The values are considered
    // to be different by default if the old value is null.
    if (newerTagTimestamp.equals(olderTagTimestamp)
      && valuesAreDifferentAndValid(olderTag, newerTag)) {
        log.debug("update() : values and timestamps are equal, so nothing to update -> reject update");
        return true;
    }

    //false means allow the update to proceed
    return false;
  }

  private static boolean valuesAreDifferentAndValid(DataTag olderTag, DataTag newerTag) {
    return olderTag.getValue() != null && newerTag.getValue().equals(olderTag.getValue())
      && olderTag.getDataTagQuality() != null && olderTag.getDataTagQuality().isValid()
      && newerTag.getDataTagQuality() != null && newerTag.getDataTagQuality().isValid();
  }
}
