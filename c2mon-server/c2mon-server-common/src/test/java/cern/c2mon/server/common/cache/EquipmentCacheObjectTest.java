package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.equipment.EquipmentCacheObject;

public class EquipmentCacheObjectTest extends CacheObjectTest<EquipmentCacheObject> {

  private static EquipmentCacheObject sample = new EquipmentCacheObject(1L);

  public EquipmentCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(EquipmentCacheObject cloneObject) {
      cloneObject.setAddress("Test address with spaces");
      cloneObject.setProcessId(1234567L);
  }
}
