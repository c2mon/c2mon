/******************************************************************************
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
 *****************************************************************************/
package cern.c2mon.shared.client.alarm.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Simple implementation of the AlarmCondition interface.
 *
 * A ValueAlarmCondition is defined for a single alarm value. If the parameter
 * passed to the evaluateState() method is equal to the defined alarm value, the
 * alarm state is supposed to be ACTIVE; if the two values differ,
 * the alarm state is supposed to be TERMINATE.
 *
 * ValueAlarmCondition is Serializable. A serialVersionUID has been defined to
 * ensure that no serialization problems occur after minor modifications to the
 * class.
 *
 * @author Jan Stowisek
 */
@Getter @Setter @NoArgsConstructor @EqualsAndHashCode(callSuper=false)
public class ValueAlarmCondition extends AlarmCondition {

  /**
   * Version number of the class used during serialization/deserialization. This
   * is to ensure that minore changes to the class do not prevent us from
   * reading back AlarmCacheObjects we have serialized earlier. If fields are
   * added/removed from the class, the version number needs to change.
   */
  private static final long serialVersionUID = -1234567L;

  /** The value for which the condition is supposed to return ACTIVE */
  private Object alarmValue;

  /**
   * Constructor
   *
   * @param alarmValue the value for which the condition is considered to return ACTIVE
   */
  public ValueAlarmCondition(final Object alarmValue) {
    setAlarmValue(alarmValue);
  }

  /**
   * @param value the value to be compared to the condition's alarm value
   *
   * @return a string representing the LASER fault state descriptor for the alarm.
   * If the value to be evaluated is null, the method will return null.
   * @throws NullPointerException  if called with null parameter
   * @throws IllegalStateException if type of alarm condition and tag value do not match
   */
  @Override
  public boolean evaluateState(final Object value) {
    if (value == null) {
      throw new NullPointerException("Trying to evaluate alarm condition for null value.");
    }

    Object enumAdaptedAlarmValue;
    if (value.getClass().isEnum() && alarmValue.getClass().equals(String.class)) {
      Class< ? extends Enum> enumClass = (Class< ? extends Enum>) value.getClass();
      enumAdaptedAlarmValue = Enum.valueOf(enumClass, (String) alarmValue);
    } else if (!value.getClass().equals(alarmValue.getClass())) {
      throw new IllegalStateException("The passed tag value type does not match the expected type for this alarm (" + value.getClass() + " != " + alarmValue.getClass() + ")");
    } else {
      enumAdaptedAlarmValue = alarmValue;
    }

    // Compare the tag value to the alarm value and determine the alarm
    // state.
    return (value.equals(enumAdaptedAlarmValue));
  }

  /**
   * @return a String representation of the object
   */
  @Override
  public String getDescription() {
    return ACTIVE + ", if value is equals to " + getAlarmValue();
  }

  @Override
  public Object clone() {
    return new ValueAlarmCondition(getAlarmValue());
  }
}
