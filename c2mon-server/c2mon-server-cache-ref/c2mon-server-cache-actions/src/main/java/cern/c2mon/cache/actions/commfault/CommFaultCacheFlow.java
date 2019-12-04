package cern.c2mon.cache.actions.commfault;

import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.server.common.commfault.CommFaultTag;

class CommFaultCacheFlow extends DefaultC2monCacheFlow<CommFaultTag> {

  CommFaultCacheFlow() {
    super((older, newer) -> newer.getEventTimestamp().getTime() >= older.getEventTimestamp().getTime());
  }
}
