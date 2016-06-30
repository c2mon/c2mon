/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.shared.daq.config;

import lombok.Data;

import javax.xml.bind.annotation.XmlValue;

/**
 * A SubEquipment unit add event.
 *
 * @author Justin Lewis Salmon
 */
@Data
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
}
