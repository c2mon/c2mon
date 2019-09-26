package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.alarm.AlarmCacheObject;

public class AlarmCacheObjectTest extends CacheObjectTest<AlarmCacheObject> {

  private static AlarmCacheObject sample = new AlarmCacheObject(1L);

  public AlarmCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(AlarmCacheObject cloneObject) {
    cloneObject.setFaultCode(123);
  }
}
