package cern.c2mon.cache.actions.datatag;

import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.datatag.DataTag;
import lombok.extern.slf4j.Slf4j;

import static cern.c2mon.cache.actions.datatag.DataTagEvaluator.filterout;

@Slf4j
class DataTagCacheFlow extends DefaultCacheFlow<DataTag> {

  DataTagCacheFlow() {
    super((older, newer) -> !filterout(older, newer));
  }
}
