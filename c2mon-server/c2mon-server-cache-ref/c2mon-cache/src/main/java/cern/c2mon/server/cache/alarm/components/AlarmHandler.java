package cern.c2mon.server.cache.alarm.components;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.tag.Tag;

/**
 * @author Szymon Halastra
 */
public interface AlarmHandler {

  void evaluateAlarm(Long alarmId);

  Alarm update(Long alarmId, Tag tag);

  String getTopicForAlarm(final Alarm alarm);
}
