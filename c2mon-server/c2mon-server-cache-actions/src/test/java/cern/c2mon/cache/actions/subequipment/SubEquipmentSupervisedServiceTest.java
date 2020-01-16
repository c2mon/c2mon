package cern.c2mon.cache.actions.subequipment;

import cern.c2mon.cache.SupervisedServiceTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.test.cache.AbstractCacheObjectFactory;
import cern.c2mon.server.test.cache.SubEquipmentCacheObjectFactory;
import lombok.Getter;

import javax.inject.Inject;

public class SubEquipmentSupervisedServiceTest extends SupervisedServiceTest<SubEquipment, SubEquipmentCacheObject> {

  @Inject
  private C2monCache<SubEquipment> equipmentCacheRef;

  @Inject
  @Getter
  private SubEquipmentService supervisedService;

  @Override
  protected C2monCache<SubEquipment> initCache() {
    return equipmentCacheRef;
  }

  @Override
  protected AbstractCacheObjectFactory<SubEquipmentCacheObject> initFactory() {
    return new SubEquipmentCacheObjectFactory();
  }
}
