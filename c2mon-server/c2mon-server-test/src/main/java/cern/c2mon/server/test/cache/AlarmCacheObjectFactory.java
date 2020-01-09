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
package cern.c2mon.server.test.cache;

import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCondition;

import java.sql.Timestamp;

public class AlarmCacheObjectFactory extends AbstractCacheObjectFactory<AlarmCacheObject> {

  @Override
  public AlarmCacheObject sampleBase() {
    AlarmCacheObject alarm1 = new AlarmCacheObject();
    AlarmCondition condition = AlarmCondition.fromConfigXML("<AlarmCondition class=\"cern.c2mon.server.common.alarm.ValueAlarmCondition\">"
      + "<alarm-value type=\"String\">DOWN</alarm-value></AlarmCondition>");
    alarm1.setId(1L);
    alarm1.setFaultFamily("fault family");
    alarm1.setFaultMember("fault member");
    alarm1.setFaultCode(0);
    alarm1.setCondition(condition);
    alarm1.setInfo("alarm info");
    alarm1.setActive(false);
    alarm1.setInternalActive(false);
    alarm1.setTriggerTimestamp(new Timestamp(0L));
    alarm1.setDataTagId(100003L);
    return alarm1;
  }

  public AlarmCacheObject alarmActiveWithFalseCondition() {
    AlarmCacheObject alarm1 = sampleBase();
    alarm1.setId(2L);
    alarm1.setFaultFamily("fault family 2");
    alarm1.setFaultMember("fault member 2");
    alarm1.setFaultCode(2);
    AlarmCondition condition = AlarmCondition.fromConfigXML("<AlarmCondition class=\"cern.c2mon.server.common.alarm.ValueAlarmCondition\">"
      + "<alarm-value type=\"Boolean\">false</alarm-value></AlarmCondition>");
    alarm1.setCondition(condition);
    alarm1.setActive(true);
    alarm1.setInternalActive(true);
    return alarm1;
  }

  public AlarmCacheObject alarmWithOtherId() {
    AlarmCacheObject alarm3 = sampleBase();
    alarm3.setId(3L);
    return alarm3;
  }
}
