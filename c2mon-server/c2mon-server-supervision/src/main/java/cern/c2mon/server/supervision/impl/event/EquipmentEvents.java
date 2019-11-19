package cern.c2mon.server.supervision.impl.event;

import cern.c2mon.server.common.equipment.Equipment;

public class EquipmentEvents implements SupervisionEventHandler<Equipment> {
  @Override
  public void onUp(Equipment supervised) {

  }

  @Override
  public void onDown(Equipment supervised) {

  }
}
