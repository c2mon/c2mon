/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2010 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.server.common.alarm;

/**
 * <b>NOTE: imported as-is into C2MON</b>
 * 
 * <p>
 * Simple implementation of the AlarmCondition interface.
 * 
 * A ValueAlarmCondition is defined for a single alarm value. If the parameter
 * passed to the evaluateState() method is equal to the defined alarm value, the
 * alarm state is supposed to be FaultState.ACTIVE; if the two values differ,
 * the alarm state is supposed to be FaultState.TERMINATE.
 * 
 * ValueAlarmCondition is Serializable. A serialVersionUID has been defined to
 * ensure that no serialization problems occur after minor modifications to the
 * class.
 * 
 * @author Jan Stowisek
 * @version $Revision: 1.8 $ ($Date: 2007/03/07 09:48:19 $ - $State: Exp $)
 */
public class ValueAlarmCondition extends AlarmCondition {

  /**
   * Version number of the class used during serialization/deserialization. This
   * is to ensure that minore changes to the class do not prevent us from
   * reading back AlarmCacheObjects we have serialized earlier. If fields are
   * added/removed from the class, the version number needs to change.
   */
  static final long serialVersionUID = -1234567L;

  /** the value for which the condition is supposed to return ACTIVE */
  protected Object alarmValue;

  /**
   * Default Constructor This constructor should only used when creating an
   * AlarmCondition object from its XML representation.
   */
  public ValueAlarmCondition() {
    // nothing to do
  }

  /**
   * Constructor
   * 
   * @param alarmValue
   *          the value for which the condition is considered to return ACTIVE
   */
  public ValueAlarmCondition(final Object pAlarmValue) {
    setAlarmValue(pAlarmValue);
  }

  /**
   * @param value
   *          the value to be compared to the condition's alarm value
   * @return a string representing the LASER fault state descriptor for the
   *         alarm. If the value to be evaluated is null, the method will return
   *         null.
   * @throws NullPointerException
   *           if called with null parameter
   * @throws IllegalStateException
   *           if type of alarm condition and tag value do not match
   */
  public String evaluateState(final Object pValue) {
    if (pValue == null) {
      throw new NullPointerException("Trying to evaluate alarm condition for null value.");
    }

    Object enumAdaptedAlarmValue;
    if (pValue.getClass().isEnum() && alarmValue.getClass().equals(String.class)) {
      Class< ? extends Enum> enumClass = (Class< ? extends Enum>) pValue.getClass();
      enumAdaptedAlarmValue = Enum.valueOf(enumClass, (String) alarmValue);
    } else if (!pValue.getClass().equals(alarmValue.getClass())) {
      throw new IllegalStateException("The passed tag value type does not match the expected type for this alarm.");
    } else {
      enumAdaptedAlarmValue = alarmValue;
    }

    // Compare the tag value to the alarm value and determine the alarm
    // state.
    if (pValue.equals(enumAdaptedAlarmValue)) {
      return AlarmCondition.ACTIVE;
    } else {
      return AlarmCondition.TERMINATE;
    }
  }

  /**
   * Set a new alarm value for the ValueAlarmCondition
   * 
   * @param alarmValue
   *          the value for which the condition is supposed to return ACTIVE
   */
  public final void setAlarmValue(final Object pAlarmValue) {
    this.alarmValue = pAlarmValue;
  }

  /**
   * Return the condition's alarm value
   * 
   * @return the value for which the condition is supposed to return ACTIVE
   */
  public final Object getAlarmValue() {
    return this.alarmValue;
  }

  /**
   * @return a String representation of the object
   */
  public String toString() {
    return this.getClass().getName() + "(alarmValue= " + getAlarmValue() + ")";
  }

  public Object clone() {
    return new ValueAlarmCondition(getAlarmValue());
  }

  public boolean equals(final Object obj) {
    if (obj instanceof ValueAlarmCondition) {
      ValueAlarmCondition cond = (ValueAlarmCondition) obj;
      if (this.alarmValue == null) {
        return cond.alarmValue == null;
      } else {
        return this.alarmValue.equals(cond.alarmValue);
      }
    } else {
      return false;
    }

  }

}
