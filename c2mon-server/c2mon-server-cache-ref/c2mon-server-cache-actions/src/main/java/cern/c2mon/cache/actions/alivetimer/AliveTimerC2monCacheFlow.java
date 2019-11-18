package cern.c2mon.cache.actions.alivetimer;

import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.server.common.alive.AliveTimer;

class AliveTimerC2monCacheFlow extends DefaultC2monCacheFlow<AliveTimer> {

  AliveTimerC2monCacheFlow() {
    super((older, newer) -> newer.getLastUpdate() >= older.getLastUpdate());
  }
}
