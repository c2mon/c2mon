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
package cern.c2mon.daq.tools;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.type.TypeConverter;

import static cern.c2mon.shared.common.type.TypeConverter.getType;

/**
 * Class with all possible validations for Data Tag Values
 *
 * @author vilches
 */
@Slf4j
public class DataTagValueValidator {

  /**
   * The maximum allowed difference between the system's timestamp and the
   * equipment's timestamp
   */
  private static final int MAX_MSECONDS_DIFF = 300000; // 5 minutes

  /**
   * Creates a new Data Tag Value Validator which uses the provided equipment
   * logger to log its results.
   */
  public DataTagValueValidator() {
  }

  /**
   * This method is responsible for checking if new value received from data
   * source fits in a proper range
   *
   * @param sdt
   *          the source data tag object
   * @param value
   *          new value of SourceDataTag to be checked
   * @return True if the value is in range else false.
   */
  @SuppressWarnings("unchecked")
  public boolean isInRange(final SourceDataTag sdt, final Object value) {
    log.trace("isInRange - entering isInRange()..");

    boolean isInRange = true;
    Comparable convertedValue;
    setDataTypeOfRangeValue(sdt);

    if (sdt.getMinValue() != null) {
      // Convert value before comparing (we assume if we get here the value is
      // convertible)
      convertedValue = (Comparable) TypeConverter.cast(value, sdt.getDataType());
      if (compare(sdt.getMinValue(), convertedValue) > 0) {
        log.trace("\tisInRange - out of range : " + convertedValue
            + " is less than the authorized minimum value " + sdt.getMinValue());
        isInRange = false;
      }
    }

    if (isInRange) {
      if (sdt.getMaxValue() != null) {
        // Convert value before comparing (we assume if we get here the value is
        // convertible)
        convertedValue = (Comparable) TypeConverter.cast(value, sdt.getDataType());
        if (compare(sdt.getMaxValue(), convertedValue) < 0) {
          log.trace("\tisInRange - out of range : " + convertedValue
              + " is greater than the authorized maximum value " + sdt.getMaxValue());
          isInRange = false;
        }
      }
    }

    log.trace("isInRange - leaving isInRange(). Is value in range?: " + isInRange);
    return isInRange;
  }

  private void setDataTypeOfRangeValue(SourceDataTag sdt) {
    Class<?> dataType = getType(sdt.getDataType());

    if (sdt.getMinValue() != null && !sdt.getMinValue().getClass().equals(dataType)) {
      sdt.setMinValue((Number) TypeConverter.castToType(sdt.getMinValue(), dataType));
    }

    if (sdt.getMaxValue() != null && !sdt.getMaxValue().getClass().equals(dataType)) {
      sdt.setMaxValue((Number) TypeConverter.castToType(sdt.getMaxValue(), dataType));
    }

  }

  public int compare(Number a, Comparable b) {
    return ((Comparable) a).compareTo(b);
  }

  /**
   * This method checks whether the equipment's time is too far in the future or
   * not. For doing that, the equipment's time is compared to the system's time
   *
   * @param timestamp
   *          Time sent from the equipment in ms.
   * @return Whether the equipment's timestamp is inside the indicated time
   *         range or not
   */
  public boolean isTimestampValid(final long timestamp) {
    log.trace("entering isTimestampValid()..");
    boolean isValid = true;
    long diff = (timestamp - System.currentTimeMillis());
    if (diff > MAX_MSECONDS_DIFF) {
      isValid = false;
    }
    log.trace("leaving isTimestampValid().. Result: " + isValid);
    return isValid;
  }

  /**
   * Checks if the tagValue is convertible to the value of the current tag.
   *
   * @param tag
   *          The tag to check.
   * @param tagValue
   *          The value to check.
   * @return <code>true</code> if the value is convertible else
   *         <code>false</code>.
   */
  public boolean isConvertible(final SourceDataTag tag, final Object tagValue) {
    log.trace("isConvertible - Tag #" + tag.getId() + " casting " + tagValue + " to " + tag.getDataType());
    return TypeConverter.isConvertible(tagValue, tag.getDataType());
  }
}
