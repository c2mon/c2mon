package cern.c2mon.cache.actions.alarm;

import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.server.common.alarm.Alarm;

class AlarmC2monCacheFlow extends DefaultC2monCacheFlow<Alarm> {

  AlarmC2monCacheFlow() {
    super((older, newer) -> newer.getSourceTimestamp().getTime() >= older.getSourceTimestamp().getTime());
  }
}
