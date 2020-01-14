package cern.c2mon.server.common.supervised;

import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;

public class SubEquipmentSupervisedTest extends SupervisedTest<SubEquipment> {
  @Override
  protected SubEquipment generateSample() {
    return new SubEquipmentCacheObject(0L);
  }
}
