package cern.c2mon.shared.daq.config;

/**
 * A SubEquipment unit remove event.
 *
 * @author Justin Lewis Salmon
 */
public class SubEquipmentUnitRemove extends Change {

  /**
   * The ID of the SubEquipment to remove.
   */
  private Long subEquipmentId;

  /**
   * The parent Equipment unique identifier
   */
  private Long parentEquipmentId;

  public SubEquipmentUnitRemove() {
  }

  /**
   * This constructor is a kind of a copy constructor. It may be used from
   * subclasses to create an object of this class for serialisation to the DAQ
   * core.
   *
   * @param subEquipmentUnitRemove the update object to copy.
   */
  public SubEquipmentUnitRemove(final SubEquipmentUnitRemove subEquipmentUnitRemove) {
    setChangeId(subEquipmentUnitRemove.getChangeId());
    setSubEquipmentId(subEquipmentUnitRemove.subEquipmentId);

  }

  /**
   * Creates a new SubEquipment unit remove event.
   *
   * @param changeId the change id of the new change.
   * @param subEquipmentId the id longof the equipment to add the data tag to.
   * @param parentEquipmentId the parent Equipment unique identifier
   */
  public SubEquipmentUnitRemove(final Long changeId, final Long subEquipmentId, final Long parentId) {
    setChangeId(changeId);
    setSubEquipmentId(subEquipmentId);
    setParentEquipmentId(parentId);
  }

  /**
   * Set the SubEquipment ID.
   *
   * @param subEquipmentId the ID to set
   */
  public void setSubEquipmentId(Long subEquipmentId) {
    this.subEquipmentId = subEquipmentId;
  }

  /**
   * Retrieve the SubEquipment ID.
   *
   * @return the ID of the SubEquipment
   */
  public Long getSubEquipmentId() {
    return subEquipmentId;
  }

  public Long getParentEquipmentId() {
    return parentEquipmentId;
  }

  public void setParentEquipmentId(Long parentEquipmentId) {
    this.parentEquipmentId = parentEquipmentId;
  }
}