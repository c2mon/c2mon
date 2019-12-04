package cern.c2mon.cache.actions.alivetimer;

import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.alive.AliveTimer;

class AliveTimerCacheFlow extends DefaultCacheFlow<AliveTimer> {

  AliveTimerCacheFlow() {
    super((older, newer) -> newer.getLastUpdate() >= older.getLastUpdate());
  }
}
