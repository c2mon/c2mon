package cern.c2mon.shared.client.configuration.configuration.process;

import java.util.ArrayList;
import java.util.List;

import cern.c2mon.shared.client.configuration.configuration.equipment.Equipment;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class Process {

  private final Long id;

  private List<Equipment> equipments = new ArrayList<>();

  /**
   *
   */
  public Process(Long id) {
    this.id = id;
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @return the equipments
   */
  public List<Equipment> getEquipments() {
    return equipments;
  }

  public void addEquipment(Equipment equipment) {
    this.equipments.add(equipment);
  }
}
