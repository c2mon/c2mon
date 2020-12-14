/*******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
 ******************************************************************************/
package cern.c2mon.shared.common.datatag.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import cern.c2mon.shared.common.datatag.DataTagAddress;

/**
 * Definition of the supported value deadband types which can be configured for numeric DataTags
 * 
 * @see {@link DataTagAddress#setValueDeadbandType(short)}
 * @author mbraeger
 *
 */
@AllArgsConstructor
public enum ValueDeadbandType {
  /** Constant to be used to disable value-based deadband filtering in a DAQ process. */
  NONE(0),
  /** 
   * Constant to be used to enable absolute value deadband filtering on the DAQ process level. When absolute value
   * deadband filtering is enabled, the DAQ process will only accept a new tag value if it is at least "deadbandValue"
   * greater or less than the last known value. Otherwise, the new value will be discarded.
   */
  PROCESS_ABSOLUTE(1),
  /**
   * Constant to be used to enable relative value deadband filtering on the DAQ process level. When absolute value
   * deadband filtering is enabled, the DAQ process will only accept a new tag value if it is at least "deadbandValue"
   * per cent (!) greater or less than the last known value. Otherwise, the new value will be discarded.
   */
  PROCESS_RELATIVE(2),
  /**
   * Constant to be used to enable absolute value deadband filtering on the equipment message handler level. When
   * absolute value deadband filtering is enabled, the message handler will only accept a new tag value if it is at
   * least "deadbandValue" greater or less than the last known value. Otherwise, the new value will be discarded. The
   * DAQ process framework will not perform any deadband filtering if this type is set.
   */
  EQUIPMENT_ABSOLUTE(3),
  /**
   * Constant to be used to enable relative value deadband filtering on the equipment message handler level. When
   * absolute value deadband filtering is enabled, the message handler will only accept a new tag value if it is at
   * least "deadbandValue" percent (!) greater or less than the last known value. Otherwise, the new value will be
   * discarded. The DAQ process framework will not perform any deadband filtering if this type is set.
   */
  EQUIPMENT_RELATIVE(4),
  /**
   * Constant to be used to enable absolute value deadband filtering on the DAQ process level. As long as value
   * description stays unchanged, it works in exactly the same fashion as {@link #PROCESS_ABSOLUTE}. If, however
   * value description change is detected, deadband filtering is skipped.
   */
  PROCESS_ABSOLUTE_VALUE_DESCR_CHANGE(5),
  /**
   * Constant to be used to enable relative value deadband filtering on the DAQ process level. As long as value
   * description stays unchanged, it works in exactly the same fashion as {@link #PROCESS_RELATIVE}. If, however
   * value description change is detected, deadband filtering is skipped.
   */
  PROCESS_RELATIVE_VALUE_DESCR_CHANGE(6);
  
  /** Unique ID, required internally by C2MON */
  @Getter
  private final Integer id;
  
  /**
   * Returns the corresponding {@link ValueDeadbandType} for the given id,
   * or refers to {@link #NONE} if no particular match can can be found.
   * 
   * @param id the reference id for a given value type
   * @return the corresponding {@link ValueDeadbandType}
   * 
   * @see #id
   */
  public static final ValueDeadbandType getValueDeadbandType(Integer id) {
    if (id != null) {
      for (ValueDeadbandType type : ValueDeadbandType.values()) {
        if (type.id.equals(id)) {
          return type;
        }
      }
    }
    return NONE;
  }
}
