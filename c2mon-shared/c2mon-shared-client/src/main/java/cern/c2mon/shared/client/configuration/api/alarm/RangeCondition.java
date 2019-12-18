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
package cern.c2mon.shared.client.configuration.api.alarm;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Range condition to a {@link Alarm}.
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
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RangeCondition<T extends Number> extends AlarmCondition {

  /**
   * Lower boundary of the alarm range. May be null. Please note that the
   * maxValue MUST be of the same type as the associated data tag.
   */
  private T minValue;

  /**
   * Upper boundary of the alarm range. May be null. Please note that the
   * maxValue MUST be of the same type as the associated data tag.
   */
  private T maxValue;
  
  /**
   * Alarm is thrown, if the value is out of range. By default this is disabled.
   */
  private boolean outOfRangeAlarm = false;

  @Builder
  public RangeCondition(T minValue, T maxValue) {
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  @Override
  public String getXMLCondition() {
    String result = "";
    result += "<AlarmCondition class=\"cern.c2mon.server.common.alarm.RangeAlarmCondition\">\n";
    result += minValue != null ? "  <min-value type=\"" + minValue.getClass().getName() + "\">" + minValue + "</min-value>\n" : "";
    result += maxValue != null ? "  <max-value type=\"" + maxValue.getClass().getName() + "\">" + maxValue + "</max-value>\n" : "";
    result += "  <out-of-range-alarm type=\"java.lang.Boolean\">" + outOfRangeAlarm + "</out-of-range-alarm>\n";
    result += "</AlarmCondition>";
    return result;
  }
}
