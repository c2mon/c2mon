package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;

public class SubEquipmentCacheObjectTest extends CacheObjectTest<SubEquipmentCacheObject> {

  private static SubEquipmentCacheObject sample = new SubEquipmentCacheObject(1L);

  public SubEquipmentCacheObjectTest() {
    super(sample);
  }
}
