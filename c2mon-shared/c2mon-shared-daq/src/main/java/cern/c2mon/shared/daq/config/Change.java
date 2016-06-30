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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * Simple class for a top level change event
 * which means has a change id.
 *
 * @author alang
 * @author Franz Ritter
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CommandTagAdd.class, name = "commandTagAdd"),
    @JsonSubTypes.Type(value = CommandTagRemove.class, name = "commandTagRemove"),
    @JsonSubTypes.Type(value = CommandTagRemove.class, name = "commandTagRemove"),
    @JsonSubTypes.Type(value = DataTagAdd.class, name = "dataTagAdd"),
    @JsonSubTypes.Type(value = DataTagRemove.class, name = "dataTagRemove"),
    @JsonSubTypes.Type(value = DataTagUpdate.class, name = "dataTagUpdate"),
    @JsonSubTypes.Type(value = EquipmentConfigurationUpdate.class, name = "equipmentConfigurationUpdate"),
    @JsonSubTypes.Type(value = EquipmentUnitAdd.class, name = "equipmentUnitAdd"),
    @JsonSubTypes.Type(value = EquipmentUnitRemove.class, name = "equipmentUnitRemove"),
    @JsonSubTypes.Type(value = ProcessConfigurationUpdate.class, name = "processConfigurationUpdate"),
    @JsonSubTypes.Type(value = SubEquipmentUnitAdd.class, name = "subEquipmentUnitAdd"),
    @JsonSubTypes.Type(value = SubEquipmentUnitRemove.class, name = "subEquipmentUnitRemove"),
})
public abstract class Change extends ChangePart implements IChange {
  /**
   * The id of this change.
   */
  private long changeId;

  /**
   * @return the changeId
   */
  @Override
  public long getChangeId() {
    return changeId;
  }

  /**
   * @param changeId the changeId to set
   */
  @Override
  public void setChangeId(final long changeId) {
    this.changeId = changeId;
  }
}
