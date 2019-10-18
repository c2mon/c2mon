package cern.c2mon.cache.actions.subequipment;

import cern.c2mon.cache.SupervisedServiceTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

public class SubEquipmentSupervisedServiceTest extends SupervisedServiceTest<SubEquipment> {

  @Autowired
  private C2monCache<SubEquipment> equipmentCacheRef;

  @Autowired
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

  @Override
  protected void mutateObject(SubEquipment cacheable) {
    cacheable.getStatusTime().setTime(10);
  }
}
