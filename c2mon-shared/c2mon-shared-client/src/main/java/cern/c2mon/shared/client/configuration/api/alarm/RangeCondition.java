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
 * Attention the getXMLCondition Method returns a hard coded xmml string.
 * The related class path in the xml String is witten in the code itself
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RangeCondition extends AlarmCondition {

  /**
   * Lower boundary of the alarm range. May be null. Please note that the
   * maxValue MUST be of the same type as the associated data tag.
   */
  private Integer minValue;

  /**
   * Upper boundary of the alarm range. May be null. Please note that the
   * maxValue MUST be of the same type as the associated data tag.
   */
  private Integer maxValue;

  public RangeCondition() {
  }

  @Builder
  public RangeCondition(Class<?> dataType, Integer minValue, Integer maxValue) {
    super(dataType);
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  @Override
  public String getXMLCondition() {
    String result = "";
    result += "<AlarmCondition class=\"cern.c2mon.server.common.alarm.RangeAlarmCondition\">\n";
    result += minValue != null ? "<min-value type=\"" + getDataType().getName() + "\">" + minValue.toString() + "</min-value>\n" : "";
    result += minValue != null ? "<max-value type=\"" + getDataType().getName() + "\">" + maxValue.toString() + "</max-value>\n" : "";
    result += "</AlarmCondition>";
    return result;
  }
}
