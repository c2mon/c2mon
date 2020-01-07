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
package cern.c2mon.server.common.alarm;

import lombok.NoArgsConstructor;

/**
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
 * @deprecated Moved to {@link cern.c2mon.shared.client.alarm.condition.ValueAlarmCondition}
 */
@Deprecated
@NoArgsConstructor
public class ValueAlarmCondition extends cern.c2mon.shared.client.alarm.condition.ValueAlarmCondition {

  /**
   * Version number of the class used during serialization/deserialization. This
   * is to ensure that minore changes to the class do not prevent us from
   * reading back AlarmCacheObjects we have serialized earlier. If fields are
   * added/removed from the class, the version number needs to change.
   */
  private static final long serialVersionUID = -1234567L;

  /**
   * Constructor
   *
   * @param pAlarmValue the value for which the condition is considered to return ACTIVE
   */
  public ValueAlarmCondition(final Object pAlarmValue) {
    super(pAlarmValue);
  }
}
