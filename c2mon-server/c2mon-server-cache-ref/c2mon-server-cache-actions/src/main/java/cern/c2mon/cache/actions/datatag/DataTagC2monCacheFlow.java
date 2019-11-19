package cern.c2mon.cache.actions.datatag;

import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.server.common.tag.InfoTag;

class DataTagC2monCacheFlow<T extends InfoTag> extends DefaultC2monCacheFlow<T> {

  DataTagC2monCacheFlow() {
    super((older, newer) -> newer.getTimestamp().getTime() >= older.getTimestamp().getTime());
  }
}
