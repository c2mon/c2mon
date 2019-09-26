package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.device.DeviceClassCacheObject;

public class DeviceClassCacheObjectTest extends CacheObjectTest<DeviceClassCacheObject> {

  private static DeviceClassCacheObject sample = new DeviceClassCacheObject(1L);

  public DeviceClassCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(DeviceClassCacheObject cloneObject) {
      cloneObject.setName("KInda secret device");
  }
}
