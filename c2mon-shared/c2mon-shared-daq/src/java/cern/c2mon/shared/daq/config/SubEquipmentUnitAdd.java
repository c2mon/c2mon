package cern.c2mon.shared.daq.config;

import javax.xml.bind.annotation.XmlValue;

/**
 * A SubEquipment unit add event.
 *
 * @author Justin Lewis Salmon
 */
public class SubEquipmentUnitAdd extends Change {

  /**
   * The SubEquipment unique identifier
   */
  private long subEquipmentId;

  /**
   * The parent Equipment unique identifier
   */
  private long parentEquipmentId;

  /**
   * The CDATA section with the SubEquipmentUnit XML block
   */
  @XmlValue
  private String subEquipmentUnitXml;

  public SubEquipmentUnitAdd() {
  }

  /**
   * This constructor is a kind of copy constructor. It may be used from subclasses
   * to create an object of this class for serialisation to the DAQ core.
   *
   * @param subEqUnitAdd the update object to copy.
   */
  public SubEquipmentUnitAdd(final SubEquipmentUnitAdd subEqUnitAdd) {
    setChangeId(subEqUnitAdd.getChangeId());
    this.subEquipmentId = subEqUnitAdd.subEquipmentId;
    this.parentEquipmentId = subEqUnitAdd.parentEquipmentId;
    this.subEquipmentUnitXml = subEqUnitAdd.subEquipmentUnitXml;
  }

  /**
   * Creates a new SubEquipment unit add change.
   *
   * @param changeId the change id of the new change.
   * @param subEquipmentId the SubEquipment identifier
   * @param parentEquipmentId the parent Equipment unique identifier
   * @param subEquipmentUnitXml the XML configuration of the SubEquipment
   *          (SubEquipmentUnit block)
   */
  public SubEquipmentUnitAdd(final Long changeId, final long subEquipmentId, final long parentEquipmentId, final String subEquipmentUnitXml) {
    setChangeId(changeId);
    this.subEquipmentId = subEquipmentId;
    this.parentEquipmentId = parentEquipmentId;
    this.subEquipmentUnitXml = subEquipmentUnitXml;
  }

  public long getSubEquipmentId() {
    return subEquipmentId;
  }

  public void setSubEquipmentId(long subEquipmentId) {
    this.subEquipmentId = subEquipmentId;
  }

  public long getParentEquipmentId() {
    return parentEquipmentId;
  }

  public void setParentEquipmentId(long parentEquipmentId) {
    this.parentEquipmentId = parentEquipmentId;
  }

  public String getSubEquipmentUnitXml() {
    return subEquipmentUnitXml;
  }

  public void setSubEquipmentUnitXml(final String subEquipmentUnitXml) {
    this.subEquipmentUnitXml = subEquipmentUnitXml;
  }

}