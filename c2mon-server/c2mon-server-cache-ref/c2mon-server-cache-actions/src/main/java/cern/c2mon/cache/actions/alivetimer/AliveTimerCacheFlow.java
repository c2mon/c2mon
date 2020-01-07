package cern.c2mon.cache.actions.alivetimer;

import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.alive.AliveTag;

class AliveTimerCacheFlow extends DefaultCacheFlow<AliveTag> {

  AliveTimerCacheFlow() {
    super((older, newer) -> newer.getLastUpdate() >= older.getLastUpdate());
  }
}
