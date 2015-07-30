package cern.c2mon.shared.client.configuration.configuration.tag;

import cern.c2mon.shared.common.datatag.DataTagAddress;


/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class DataTag {

  private final Long id;

  private final String name;

  private final String description;

  private final Long equipmentId;

  private final String type;

  private final Boolean control;

  private final DataTagAddress address;

  private Boolean delete = false;

  /**
   *
   */
  public DataTag(Long id, String name, String description, Long equipmentId, String type, Boolean control, DataTagAddress address) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.equipmentId = equipmentId;
    this.type = type;
    this.control = control;
    this.address = address;
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return the equipmentId
   */
  public Long getEquipmentId() {
    return equipmentId;
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @return
   */
  public Boolean isControl() {
    return this.control;
  }

  /**
   * @return the address
   */
  public DataTagAddress getAddress() {
    return address;
  }

  /**
   * @return the delete
   */
  public Boolean getDelete() {
    return delete;
  }

  /**
   * @param delete the delete to set
   */
  public void setDelete(Boolean delete) {
    this.delete = delete;
  }
}
