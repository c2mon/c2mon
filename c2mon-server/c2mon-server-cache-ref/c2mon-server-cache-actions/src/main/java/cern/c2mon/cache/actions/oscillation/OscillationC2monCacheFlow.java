package cern.c2mon.cache.actions.oscillation;

import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.alarm.OscillationTimestamp;

class OscillationC2monCacheFlow extends DefaultCacheFlow<OscillationTimestamp> {

  OscillationC2monCacheFlow() {
    super((older, newer) -> newer.getTimeInMillis() >= older.getTimeInMillis());
  }
}
