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
package cern.c2mon.shared.common.datatag.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Contains the data quality information code sent by a DAQ module
 *
 * @author Matthias Braeger
 */
@Getter
@AllArgsConstructor
public enum SourceDataTagQualityCode {

  /** Quality code representing a VALID SourceDataTag value. This one is set automatically to a normal tag update */
  OK(0, "OK"),
  /**
   * Quality code representing a SourceDataTag value that is outside
   * the min/max range defined for the DataTag.
   * </br>
   * Used internally by the DAQ core.
   */
  OUT_OF_BOUNDS(1, "Value out of range"),
  /**
   * Quality code representing a SourceDataTag value that has been
   * corrupted before it was received by the DAQ.
   */
  VALUE_CORRUPTED(2, "Corrupted source value"),
  /**
   * This quality code must be set, if source data cannot be converted.
   * into the configured tag data type (for example: double -> boolean)
   */
  CONVERSION_ERROR(3, "Data type conversion error"),
  /**
   * Quality code representing a SourceDataTag value that is currently not
   * available from the source. <br>
   * Please use that error code with care, because it implicitly allows
   * that it gets overwritten in the server cache by an update with an older
   * time stamp.
   *
   * @see cern.c2mon.shared.common.datatag.DataTagQuality#INACCESSIBLE
   * @see cern.c2mon.shared.common.datatag.DataTagCacheObject
   */
  DATA_UNAVAILABLE(4, "Value unavailable"),
  /**
   * Quality code representing a SourceDataTag value that is invalid for
   * an unknown reason or for a reason not covered by the other quality
   * codes.
   */
  UNKNOWN(5, "Unknown"),
  /**
   * Quality code representing a SourceDataTag value that cannot be decoded
   * because the data type sent by the source is not supported by the handler.
   */
  UNSUPPORTED_TYPE(6, "Unsupported source data type"),
  /**
   * Quality code representing a SourceDataTag value that cannot be acquired
   * because of an error in the tag's hardware address.
   */
  INCORRECT_NATIVE_ADDRESS(7, "Incorrect hardware address"),
  /**
   * Quality code representing a SourceDataTag value that is received from the equipment with
   * source timestamp set in the future in relation to the DAQ time, at the moment of acquisition
   */
  FUTURE_SOURCE_TIMESTAMP(8, "Source timestamp in the future"),

  /**
   * Quality code representing a SourceDataTag value which did not receive a
   * update in a given time.
   */
  STALE(9, "Value is stale");


  /** quality code sent by the data source */
  private final int qualityCode;

  /** optional detailed quality description from the data source */
  private final String description;

  /**
   * @return the correct enum representation for the given quality code number
   * @deprecated Soon no longer supported
   */
  @Deprecated
  public static final SourceDataTagQualityCode getEnum(short code) {
    for (SourceDataTagQualityCode codeEnum : SourceDataTagQualityCode.values()) {
      if (codeEnum.qualityCode == (int) code) {
        return codeEnum;
      }
    }
    
    return SourceDataTagQualityCode.UNKNOWN;
  }
}
