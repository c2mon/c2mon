package cern.c2mon.server.common.supervised;

import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;

public class EquipmentSupervisedTest extends AbstractSupervisedCacheObjectTest<Equipment> {
  @Override
  protected Equipment generateSample() {
    return new EquipmentCacheObject();
  }
}
