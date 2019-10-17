package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;

public class SubEquipmentCacheObjectTest extends CacheableTest<SubEquipmentCacheObject> {

  private static SubEquipmentCacheObject sample = new SubEquipmentCacheObject(1L);

  public SubEquipmentCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(SubEquipmentCacheObject cloneObject) {
      cloneObject.setParentId(123L);
      cloneObject.setDescription("Test");
  }
}
