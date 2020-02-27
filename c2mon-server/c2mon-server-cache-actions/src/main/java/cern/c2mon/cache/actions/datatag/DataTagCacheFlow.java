package cern.c2mon.cache.actions.datatag;

import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.datatag.DataTag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class DataTagCacheFlow extends DefaultCacheFlow<DataTag> {

  DataTagCacheFlow() {
    super(DataTagEvaluator::allowUpdate);
  }
}
