package cern.c2mon.client.ext.history.alarm.impl;

import cern.c2mon.client.ext.history.alarm.Alarm;
import cern.c2mon.client.ext.history.alarm.AlarmHistoryService;
import cern.c2mon.client.ext.history.alarm.HistoricAlarmQuery;
import cern.c2mon.client.ext.history.alarm.repository.AlarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
@Service
public class AlarmHistoryServiceImpl implements AlarmHistoryService {

  @Autowired
  private AlarmRepository alarmRepository;

  @Override
  public List<Alarm> findBy(HistoricAlarmQuery query) {
    return toList(alarmRepository.findAll(query.getPredicate()));
  }

  @Override
  public Page<Alarm> findBy(HistoricAlarmQuery query, PageRequest page) {
    return alarmRepository.findAll(query.getPredicate(), page);
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
