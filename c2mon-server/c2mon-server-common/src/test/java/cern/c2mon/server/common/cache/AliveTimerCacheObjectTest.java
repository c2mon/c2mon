package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.alive.AliveTimerCacheObject;

public class AliveTimerCacheObjectTest extends CacheObjectTest<AliveTimerCacheObject> {

  private static AliveTimerCacheObject sample = new AliveTimerCacheObject(1L);

  public AliveTimerCacheObjectTest() {
    super(sample);
  }
}
