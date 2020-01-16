package cern.c2mon.cache.actions.supervision;

import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.supervision.Supervised;

public class AbstractSupervisedCacheFlow<T extends Supervised> extends DefaultCacheFlow<T> {

  public AbstractSupervisedCacheFlow() {
    super((older, newer) ->
      (older.getStatusTime() == null || newer.getStatusTime() == null)
        || newer.getStatusTime().getTime() >= older.getStatusTime().getTime());
  }
}
