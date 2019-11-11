package cern.c2mon.cache.actions.tag;

import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.server.common.tag.InfoTag;

public class InfoTagC2monCacheFlow<T extends InfoTag> extends DefaultC2monCacheFlow<T> {

  public InfoTagC2monCacheFlow() {
    super((older, newer) -> newer.getTimestamp().getTime() >= older.getTimestamp().getTime());
  }
}
