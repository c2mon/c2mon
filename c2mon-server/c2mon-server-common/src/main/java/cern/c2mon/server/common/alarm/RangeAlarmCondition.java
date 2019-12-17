/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.common.alarm;

import cern.c2mon.shared.common.type.TypeConverter;

/**
 * <b>NOTE: imported as-is into C2MON</b>
 *
 * Implementation of the AlarmCondition interface.
 *
 * A RangeAlarmCondition is defined for alarms that are to be activated if the
 * value of the associated DataTag is in a defined range( min <= value <= max)
 *
 * If the parameter passed to the evaluateState() method is greater than or
 * equal to the defined minimum value AND less than or equal to the defined
 * maximum value, the alarm state is supposed to be FaultState.ACTIVE; If the
 * value is outside the defined range, the alarm state is supposed to be
 * FaultState.TERMINATE.
 *
 * If either the minimum value or the maximum value are null, the condition is
 * checked for an open range (e.g. value >= min OR value <= max).
 *
 * RangeAlarmCondition is Serializable. A serialVersionUID has been defined to
 * ensure that no serialization problems occur after minor modifications to the
 * class.
 *
 * @author Jan Stowisek, Matthias Braeger
 */
public class RangeAlarmCondition<T extends Number & Comparable<T>> extends AlarmCondition {

  /**
   * Version number of the class used during serialization/deserialization. This
   * is to ensure that minore changes to the class do not prevent us from
   * reading back AlarmConditions we have serialized earlier. If fields are
   * added/removed from the class, the version number needs to change.
   */
  static final long serialVersionUID = -1234567L;

  /**
   * Lower boundary of the alarm range. May be null. Please note that the
   * maxValue MUST be of the same type as the associated data tag.
   */
  protected Comparable minValue;

  /**
   * Upper boundary of the alarm range. May be null. Please note that the
   * maxValue MUST be of the same type as the associated data tag.
   */
  protected Comparable maxValue;

  /**
   * Default Constructor This constructor should only used when creating an
   * AlarmCondition object from its XML representation.
   */
  protected RangeAlarmCondition() {
    // nothing to do
  }

  /**
   * Constructor
   *
   * @param pMin
   *          lower limit of the alarm range (may be null)
   * @param pMax
   *          upper limit of the alarm range (may be null)
   */
  public RangeAlarmCondition(final Comparable<T> pMin, final Comparable<T> pMax) {
    this.minValue = pMin;
    this.maxValue = pMax;
  }

  /**
   * Implementation of the AlarmCondition interface
   *
   * @param value
   *          the current value of the associated DataTag
   * @return ACTIVE if the value means that the alarm should be activated,
   *         TERMINATE if the value means that the alarm should be terminated.
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean evaluateState(final Object value) {
    // If the value is null, the alarm will always be terminated
    boolean result = (value != null);

    // Check for the lower boundary
    if (this.minValue != null) {
      Object castValue = TypeConverter.castToType(value, minValue.getClass());
      result = result && this.minValue.getClass().equals(castValue.getClass()) && minValue.compareTo(castValue) <= 0;
    }

    // Check for the upper boundary
    if (this.maxValue != null) {
      Object castValue = TypeConverter.castToType(value, maxValue.getClass());
      result = result && this.maxValue.getClass().equals(castValue.getClass()) && maxValue.compareTo(castValue) >= 0;
    }

    return result;
  }

  /**
   * Get the lower range limit for the alarm condition. This method will return
   * null if no lower limit is defined
   *
   * @return the lower range limit for the alarm condition.
   */
  @SuppressWarnings("unchecked")
  public Comparable<T> getMinimumValue() {
    return this.minValue;
  }

  /**
   * Get the upper range limit for the alarm condition. This method will return
   * null if no upper limit is defined
   *
   * @return the upper range limit for the alarm condition.
   */
  @SuppressWarnings("unchecked")
  public Comparable<T> getMaximumValue() {
    return this.maxValue;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object clone() {
    return new RangeAlarmCondition<T>(this.minValue, this.maxValue);
  }

  /**
   * @return a String representation of the object
   */
  @Override
  public String toString() {
    StringBuilder str = new StringBuilder("ACTIVE if the tag value is");
    if (this.minValue != null) {
      str.append(" >= ");
      str.append(this.minValue);
      if (this.maxValue != null) {
        str.append(" and ");
      } else {
        str.append(".");
      }
    }
    if (this.maxValue != null) {
      str.append("<= ");
      str.append(this.maxValue);
      str.append(".");
    }
    return str.toString();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(final Object obj) {
    boolean result = true;

    if (obj instanceof RangeAlarmCondition) {
      RangeAlarmCondition<T> cond = (RangeAlarmCondition<T>) obj;

      if (this.minValue == null) {
        result = cond.minValue == null;
      } else {
        result = this.minValue.equals(cond.minValue);
      }

      if (this.maxValue == null) {
        result = result && (cond.maxValue == null);
      } else {
        result = result && this.maxValue.equals(cond.maxValue);
      }
    } else {
      result = false;
    }
    return result;

  }

}
