package cern.c2mon.cache.actions.alivetimer;

import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.server.common.alive.AliveTimer;

public class AliveTimerC2monCacheFlow extends DefaultC2monCacheFlow<AliveTimer> {

  @Override
  public boolean preInsertValidate(AliveTimer older, AliveTimer newer) {
    return super.preInsertValidate(older, newer) &&
      (older == null ||
        (newer.getClass() == older.getClass() && newer.getLastUpdate() >= older.getLastUpdate())
      );
  }
}
