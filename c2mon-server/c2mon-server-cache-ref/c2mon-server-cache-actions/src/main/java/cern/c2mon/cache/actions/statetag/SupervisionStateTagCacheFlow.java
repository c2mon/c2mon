package cern.c2mon.cache.actions.statetag;

import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.supervision.SupervisionStateTag;

class SupervisionStateTagCacheFlow extends DefaultCacheFlow<SupervisionStateTag> {

  SupervisionStateTagCacheFlow() {
    super((older, newer) -> newer.getStatusTime().getTime() >= older.getStatusTime().getTime());
  }
}
