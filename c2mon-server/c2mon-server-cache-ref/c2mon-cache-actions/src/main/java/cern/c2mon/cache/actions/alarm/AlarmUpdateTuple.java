package cern.c2mon.cache.actions.alarm;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.tag.Tag;

import java.util.HashMap;
import java.util.List;

// TODO Replace uses of TagWithAlarms to this
public class AlarmUpdateTuple extends HashMap.SimpleEntry<Tag, List<Alarm>> {

  public AlarmUpdateTuple(Tag tag, List<Alarm> alarms) {
    super(tag, alarms);
  }
}
