package cern.c2mon.client.ext.history.alarm;

import java.util.List;

/**
 * This service allows querying {@link Alarm} history from the c2mon history database.
 *
 * @author Justin Lewis Salmon
 */
public interface AlarmHistoryService {

  /**
   * Returns all results matching the given {@link HistoricAlarmQuery}.
   *
   * @param query the {@link HistoricAlarmQuery} to match
   *
   * @return the matched results
   */
  List<Alarm> findBy(HistoricAlarmQuery query);

  /**
   * Returns the first maxResults results matching the given {@link HistoricAlarmQuery}.
   *
   * @param query      the {@link HistoricAlarmQuery} to match
   * @param maxResults the result limit
   *
   * @return the matched results
   */
  List<Alarm> findBy(HistoricAlarmQuery query, int maxResults);
}
