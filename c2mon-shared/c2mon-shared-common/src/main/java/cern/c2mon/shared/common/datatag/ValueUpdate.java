/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.shared.common.datatag;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Required on the DAQ side to send a value update
 * @author Matthias Braeger
 */
@AllArgsConstructor
@Data
public class ValueUpdate {
  /** The new tag value */
  private Object value;

  /** An optional description for the value update */
  private String valueDescription = "";

  /** The source timestamp of the value update in milliseconds */
  private long sourceTimestamp = System.currentTimeMillis();

  public ValueUpdate(final Object value) {
    this.value = value;
  }

  public ValueUpdate(final Object value, long sourceTimestamp) {
    this.value = value;
    this.sourceTimestamp = sourceTimestamp;
  }
}
