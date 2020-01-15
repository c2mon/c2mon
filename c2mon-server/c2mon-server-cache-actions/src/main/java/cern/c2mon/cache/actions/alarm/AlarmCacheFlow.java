package cern.c2mon.cache.actions.alarm;

import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.alarm.Alarm;

class AlarmCacheFlow extends DefaultCacheFlow<Alarm> {

  AlarmCacheFlow() {
    super((older, newer) -> newer.getSourceTimestamp().getTime() >= older.getSourceTimestamp().getTime());
  }
}
