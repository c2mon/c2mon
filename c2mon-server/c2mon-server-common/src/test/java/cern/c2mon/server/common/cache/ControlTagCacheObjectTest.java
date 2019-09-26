package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.alarm.AlarmCacheObject;

public class ControlTagCacheObjectTest extends CacheObjectTest<AlarmCacheObject> {

  private static AlarmCacheObject sample = new AlarmCacheObject(1L);

  public ControlTagCacheObjectTest() {
    super(sample);
  }
}
