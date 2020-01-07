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

import lombok.NoArgsConstructor;

/**
 * Implementation of the AlarmCondition interface.
 * <p/>
 * A RangeAlarmCondition is defined for alarms that are to be activated if the
 * value of the associated DataTag is in a defined range( min <= value <= max)
 * <p/>
 * If the parameter passed to the evaluateState() method is greater than or
 * equal to the defined minimum value AND less than or equal to the defined
 * maximum value, the alarm state is supposed to be FaultState.ACTIVE; If the
 * value is outside the defined range, the alarm state is supposed to be
 * FaultState.TERMINATE.
 * <p/>
 * If either the minimum value or the maximum value are null, the condition is
 * checked for an open range (e.g. value >= min OR value <= max).
 * <p/>
 * The logic can be inverted with the {@link #isOutOfRangeAlarm()} flag set to <code>true</code>
 * <p/>
 * RangeAlarmCondition is Serializable. A serialVersionUID has been defined to
 * ensure that no serialization problems occur after minor modifications to the
 * class.
 *
 * @author Jan Stowisek, Matthias Braeger
 * @deprecated This class was moved to {@link cern.c2mon.shared.client.alarm.condition.RangeAlarmCondition}
 */
@Deprecated @NoArgsConstructor
public class RangeAlarmCondition<T extends Number & Comparable<T>> extends cern.c2mon.shared.client.alarm.condition.RangeAlarmCondition<T> {

  /**
   * Version number of the class used during serialization/deserialization. This
   * is to ensure that minore changes to the class do not prevent us from
   * reading back AlarmConditions we have serialized earlier. If fields are
   * added/removed from the class, the version number needs to change.
   */
  private static final long serialVersionUID = -1234567L;

  /**
   * Constructor
   *
   * @param pMin
   *          lower limit of the alarm range (may be null)
   * @param pMax
   *          upper limit of the alarm range (may be null)
   */
  public RangeAlarmCondition(final Comparable<T> pMin, final Comparable<T> pMax) {
    super(pMin, pMax);
  }
}
