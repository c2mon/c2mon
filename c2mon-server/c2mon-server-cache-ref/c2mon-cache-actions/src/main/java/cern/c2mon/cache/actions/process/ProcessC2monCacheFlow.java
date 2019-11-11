package cern.c2mon.cache.actions.process;

import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.server.common.process.Process;

class ProcessC2monCacheFlow extends DefaultC2monCacheFlow<Process> {

  ProcessC2monCacheFlow() {
    super((older, newer) -> newer.getStatusTime().getTime() >= older.getStatusTime().getTime());
  }
}
