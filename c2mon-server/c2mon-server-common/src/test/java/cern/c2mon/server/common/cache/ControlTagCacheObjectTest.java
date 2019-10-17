package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.alarm.AlarmCacheObject;

public class ControlTagCacheObjectTest extends CacheableTest<AlarmCacheObject> {

  private static AlarmCacheObject sample = new AlarmCacheObject(1L);

  public ControlTagCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(AlarmCacheObject cloneObject) {
      cloneObject.setFirstOscTS(12345);
      cloneObject.setInternalActive(false);
  }
}
