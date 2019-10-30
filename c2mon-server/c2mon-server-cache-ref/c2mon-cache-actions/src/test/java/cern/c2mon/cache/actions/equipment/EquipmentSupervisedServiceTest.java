package cern.c2mon.cache.actions.equipment;

import cern.c2mon.cache.SupervisedServiceTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import lombok.Getter;

import javax.inject.Inject;

public class EquipmentSupervisedServiceTest extends SupervisedServiceTest<Equipment> {

  @Inject
  private C2monCache<Equipment> equipmentCacheRef;

  @Inject
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
