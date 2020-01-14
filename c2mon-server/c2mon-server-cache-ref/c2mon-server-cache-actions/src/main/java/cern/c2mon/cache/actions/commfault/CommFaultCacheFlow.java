package cern.c2mon.cache.actions.commfault;

import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.commfault.CommFaultTag;

class CommFaultCacheFlow extends DefaultCacheFlow<CommFaultTag> {

  CommFaultCacheFlow() {
    super((older, newer) -> newer.getSourceTimestamp().getTime() >= older.getSourceTimestamp().getTime());
  }
}
