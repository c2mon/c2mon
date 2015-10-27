package cern.c2mon.client.ext.history.alarm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
   * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code PageRequest} object and matching the given {@link
   * HistoricAlarmQuery}.
   * *
   * <p>
   * Note: pages are 0-based, i.e. asking for the 0th page will get you the first page.
   * </p>
   *
   * @param query the {@link HistoricAlarmQuery} to match
   * @param page  the paging restriction specifier
   *
   * @return a page of matched results
   */
  Page<Alarm> findBy(HistoricAlarmQuery query, PageRequest page);
}
