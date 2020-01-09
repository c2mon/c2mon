package cern.c2mon.server.supervision.impl.event;

import cern.c2mon.cache.actions.subequipment.SubEquipmentService;
import cern.c2mon.server.common.subequipment.SubEquipment;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Slf4j
@Named
@Singleton
public class SubEquipmentEvents extends SupervisionEventHandler<SubEquipment> {

  @Inject
  public SubEquipmentEvents(SubEquipmentService subEquipmentService) {
    super(SubEquipment.class, subEquipmentService);
  }
}
