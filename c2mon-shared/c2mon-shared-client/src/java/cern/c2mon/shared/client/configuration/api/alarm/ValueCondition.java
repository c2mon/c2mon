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
import lombok.ToString;

/**
 * Value condition to a {@link Alarm}.
 * <p/>
 * Attention the getXMLCondition Method returns a hard coded xmml string.
 * The related class path in the xml String is witten in the code itself
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ValueCondition extends AlarmCondition {

  private Object value;

  @Builder
  public ValueCondition(Class<?> dataType, Object value) {
    super(dataType);
    this.value = value;
  }

  public ValueCondition() {
  }

  @Override
  public String getXMLCondition() {
    String result = "";
    result += "<AlarmCondition class=\"cern.c2mon.server.common.alarm.ValueAlarmCondition\">\n";
    result += "<alarm-value type=\"" + getDataType().getName() + "\">" + value.toString() + "</alarm-value>\n";
    result += "</AlarmCondition>";
    return result;
  }

}
