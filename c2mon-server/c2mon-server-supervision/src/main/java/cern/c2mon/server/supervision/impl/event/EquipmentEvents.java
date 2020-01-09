package cern.c2mon.server.supervision.impl.event;

import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.server.common.equipment.Equipment;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Slf4j
@Named
@Singleton
public class EquipmentEvents extends SupervisionEventHandler<Equipment> {

  @Inject
  public EquipmentEvents(EquipmentService equipmentService) {
    super(Equipment.class, equipmentService);
  }
}
