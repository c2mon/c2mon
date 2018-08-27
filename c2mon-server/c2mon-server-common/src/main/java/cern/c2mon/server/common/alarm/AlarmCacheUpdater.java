package cern.c2mon.server.common.alarm;

import cern.c2mon.server.common.tag.Tag;

public interface AlarmCacheUpdater {
  Alarm update(final Alarm alarm, final Tag tag);
}
