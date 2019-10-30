package cern.c2mon.cache.actions.subequipment;

import cern.c2mon.cache.SupervisedServiceTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import lombok.Getter;

import javax.inject.Inject;

public class SubEquipmentSupervisedServiceTest extends SupervisedServiceTest<SubEquipment> {

  @Inject
  private C2monCache<SubEquipment> equipmentCacheRef;

  @Inject
  @Getter
  private SubEquipmentService supervisedService;

  @Override
  protected SubEquipment getSample() {
    return new SubEquipmentCacheObject(1L);
  }

  @Override
  protected C2monCache<SubEquipment> initCache() {
    return equipmentCacheRef;
  }
}
