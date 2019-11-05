package cern.c2mon.cache.actions.alarm;

import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.server.common.alarm.Alarm;

public class AlarmC2monCacheFlow extends DefaultC2monCacheFlow<Alarm> {

  @Override
  public boolean preInsertValidate(Alarm older, Alarm newer) {
    return older == null ||
      (super.preInsertValidate(older, newer)
        && newer.getSourceTimestamp().getTime() >= older.getSourceTimestamp().getTime());
  }
}