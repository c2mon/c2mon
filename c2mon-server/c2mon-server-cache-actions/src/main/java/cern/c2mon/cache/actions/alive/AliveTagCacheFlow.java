package cern.c2mon.cache.actions.alive;

import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.alive.AliveTag;

class AliveTagCacheFlow extends DefaultCacheFlow<AliveTag> {

  AliveTagCacheFlow() {
    super((older, newer) -> newer.getLastUpdate() >= older.getLastUpdate());
  }
}
