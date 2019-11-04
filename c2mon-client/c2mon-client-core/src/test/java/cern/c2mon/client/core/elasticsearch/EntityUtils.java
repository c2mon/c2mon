/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.client.core.elasticsearch;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCondition;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

/**
 * @author Justin Lewis Salmon
 */
public class EntityUtils {

  public static DataTagCacheObject createDataTag1() {
    DataTagCacheObject tag = new DataTagCacheObject(1L, "cpu.loadavg", Long.class.getName(), DataTagConstants.MODE_OPERATIONAL);
    tag.setProcessId(50L);
    tag.setEquipmentId(150L);
    tag.setDescription("Average CPU usage");
    tag.setAddress(new DataTagAddress());
    tag.setMaxValue(100L);
    tag.setMinValue(0L);
    tag.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    tag.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
    tag.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    tag.setDataTagQuality(new DataTagQualityImpl());
    tag.setLogged(true);
    Metadata metadata = new Metadata();
    metadata.addMetadata("building", "1");
    metadata.addMetadata("array", Collections.singletonList("test"));
    metadata.addMetadata("responsiblePerson", "Fred");
    tag.setMetadata(metadata);
    return tag;
  }

  public static List<Alarm> createAlarmsForTag(Long tagId) {
    List<Alarm> alarmList = new ArrayList<>();
    Random random = new Random();

    AlarmCacheObject alarm1 = new AlarmCacheObject(random.nextLong());
    alarm1.setDataTagId(tagId);
    alarm1.setFaultFamily("fault family");
    alarm1.setFaultMember("fault member");
    alarm1.setFaultCode(223);
    alarm1.setCondition(AlarmCondition
         .fromConfigXML("<AlarmCondition class=\"cern.c2mon.server.common.alarm.ValueAlarmCondition\"><alarm-value type=\"Boolean\">true</alarm-value></AlarmCondition>"));
    Metadata metadata1 = new Metadata();
    metadata1.addMetadata("meta1", 1);
    metadata1.addMetadata("meta2", "value2");
    metadata1.addMetadata("meta3", "value3");
    alarm1.setMetadata(metadata1);
    alarm1.setTimestamp(new Timestamp(System.currentTimeMillis()));
    alarm1.setSourceTimestamp(new Timestamp(alarm1.getTimestamp().getTime()));
    alarmList.add(alarm1);


    AlarmCacheObject alarm2 = new AlarmCacheObject(random.nextLong());
    alarm2.setDataTagId(tagId);
    alarm2.setFaultFamily("fault family 2");
    alarm2.setFaultMember("fault member 2");
    alarm2.setFaultCode(223);
    alarm2.setCondition(AlarmCondition
         .fromConfigXML("<AlarmCondition class=\"cern.c2mon.server.common.alarm.ValueAlarmCondition\"><alarm-value type=\"Boolean\">true</alarm-value></AlarmCondition>"));
    Metadata metadata2 = new Metadata();
    metadata2.addMetadata("meta4", 4);
    metadata2.addMetadata("meta5", "value5");
    metadata2.addMetadata("meta6", "value6");
    alarm2.setMetadata(metadata2);
    alarm2.setTimestamp(new Timestamp(System.currentTimeMillis()));
    alarm2.setSourceTimestamp(new Timestamp(alarm2.getTimestamp().getTime()));
    alarmList.add(alarm2);
    return alarmList;
  }

  public static DataTagCacheObject createDataTag2() {
    DataTagCacheObject tag = new DataTagCacheObject(2L, "cpu.temperature", Long.class.getName(), DataTagConstants.MODE_OPERATIONAL);
    tag.setProcessId(50L);
    tag.setEquipmentId(150L);
    tag.setDescription("CPU temperature");
    tag.setAddress(new DataTagAddress());
    tag.setMaxValue(100L);
    tag.setMinValue(-100L);
    tag.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    tag.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
    tag.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    tag.setDataTagQuality(new DataTagQualityImpl());
    tag.setLogged(true);
    Metadata metadata = new Metadata();
    metadata.addMetadata("building", "2");
    metadata.addMetadata("testLong", Long.valueOf(123L));
    metadata.addMetadata("responsiblePerson", "Bill");
    tag.setMetadata(metadata);
    return tag;
  }

  public static SupervisionEvent createSupervisionEvent() {
    return new SupervisionEventImpl(SupervisionConstants.SupervisionEntity.PROCESS, 50L,
        "P_TEST01", SupervisionConstants.SupervisionStatus.DOWN, new Timestamp(System.currentTimeMillis()), "");
  }
}
