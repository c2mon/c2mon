package cern.c2mon.client.ext.history.alarm.impl;

import cern.c2mon.client.ext.history.alarm.Alarm;
import cern.c2mon.client.ext.history.alarm.AlarmHistoryService;
import cern.c2mon.client.ext.history.alarm.HistoricAlarmQuery;
import cern.c2mon.client.ext.history.alarm.repository.AlarmRepository;
import cern.c2mon.client.ext.history.dbaccess.util.TimeZoneUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Justin Lewis Salmon
 */
@Service
public class AlarmHistoryServiceImpl implements AlarmHistoryService {

  @Autowired
  private AlarmRepository alarmRepository;

  @Override
  public List<Alarm> findBy(HistoricAlarmQuery query) {
    return toList(localise(alarmRepository.findAll(query.getPredicate())));
  }

  @Override
  public Page<Alarm> findBy(HistoricAlarmQuery query, PageRequest page) {
    return (Page<Alarm>) localise(alarmRepository.findAll(query.getPredicate(), page));
  }

  /**
   * Modify the timestamps of the given {@link Iterable<Alarm>} list to match the current local timezone.
   * Relies on the fact that the {@link Alarm} timestamps are stored as UTC.
   *
   * @param alarms the list of alarms to modify
   * @return the same list, with localised timestamps (modified in-place)
   */
  private Iterable<Alarm> localise(Iterable<Alarm> alarms) {
    for (Alarm alarm : alarms) {
      alarm.setTimestamp(TimeZoneUtil.convertDateTimezone(TimeZone.getDefault(), alarm.getTimestamp(), TimeZone.getTimeZone("UTC")));
    }

    return alarms;
  }

  /**
   * Convert an {@link Iterable} to an {@link ArrayList}.
   *
   * @param iterable
   * @param <E>
   * @return
   */
  private static <E> List<E> toList(Iterable<E> iterable) {
    List<E> list = new ArrayList<>();
    for (E item : iterable) {
      list.add(item);
    }
    return list;
  }
}
