package cern.c2mon.client.ext.history.alarm;

import java.sql.Timestamp;
import java.util.List;

/**
 * This service allows querying {@link Alarm} history from the c2mon history database.
 *
 * @author Justin Lewis Salmon
 */
public interface AlarmHistoryService {

  /**
   * Retrieve all historic alarms between the given start and end points in time.
   *
   * @param start the starting point in time
   * @param end   the end point in time
   *
   * @return all alarms between the start and end points
   */
  List<Alarm> findByTimestampBetween(Timestamp start, Timestamp end);
}
