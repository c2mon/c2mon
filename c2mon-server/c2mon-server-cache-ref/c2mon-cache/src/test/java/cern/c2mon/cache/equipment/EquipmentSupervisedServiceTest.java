package cern.c2mon.cache.equipment;

import cern.c2mon.cache.SupervisedServiceTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.equipment.EquipmentService;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

public class EquipmentSupervisedServiceTest extends SupervisedServiceTest<Equipment> {

  @Autowired
  private C2monCache<Equipment> equipmentCacheRef;

  @Autowired
  @Getter
  private EquipmentService supervisedService;

  @Override
  protected Equipment getSample() {
    return new EquipmentCacheObject(1L);
  }

  @Override
  protected C2monCache<Equipment> initCache() {
    return equipmentCacheRef;
  }
}
