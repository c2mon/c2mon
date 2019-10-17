package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.impl.HardwareAddressImpl;

public class DataTagCacheObjectTest extends CacheableTest<DataTagCacheObject> {

  private static DataTagCacheObject sample = new DataTagCacheObject(1L);

  public DataTagCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(DataTagCacheObject cloneObject) {
      cloneObject.setEquipmentId(1234L);
      cloneObject.setAddress(new DataTagAddress(new HardwareAddressImpl()));
  }
}
