package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.device.DeviceCacheObject;

public class DeviceCacheObjectTest extends CacheObjectTest<DeviceCacheObject> {

  private static DeviceCacheObject sample = new DeviceCacheObject(1L);

  public DeviceCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(DeviceCacheObject cloneObject) {
      cloneObject.setName("Top secret device");
      cloneObject.setDeviceClassId(123L);
  }
}
