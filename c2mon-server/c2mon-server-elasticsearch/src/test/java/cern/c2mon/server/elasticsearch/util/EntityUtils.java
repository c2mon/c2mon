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
package cern.c2mon.server.elasticsearch.util;

import java.sql.Timestamp;
import java.util.Collections;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.datatag.DataTag;
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

  private EntityUtils() {
    // only static methods below
  }

  public static Alarm createAlarm() {
    AlarmCacheObject alarm = new AlarmCacheObject();
    alarm.setId(1L);
    alarm.setDataTagId(2L);
    alarm.setFaultFamily("fault family");
    alarm.setFaultMember("fault member");
    alarm.setFaultCode(0);
    alarm.setTimestamp(new Timestamp(System.currentTimeMillis()));
    alarm.setSourceTimestamp(new Timestamp(alarm.getTimestamp().getTime()));
    Metadata metadata = new Metadata();
    metadata.addMetadata("building", "1");
    metadata.addMetadata("array", Collections.singletonList("test"));
    metadata.addMetadata("responsiblePerson", "Fred");
    alarm.setMetadata(metadata);
    alarm.setInfo("alarm info");
    alarm.setActive(false);
    alarm.setInternalActive(false);
    return alarm;
  }

  public static DataTag createDataTag() {
    DataTagCacheObject tag = new DataTagCacheObject(1L, "cpu.loadavg", Long.class.getName(), DataTagConstants.MODE_OPERATIONAL);
    tag.setProcessId(50L);
    tag.setEquipmentId(150L);
    tag.setDescription("Average CPU usage");
    tag.setAddress(new DataTagAddress());
    tag.setMaxValue(0);
    tag.setMinValue(4.0);
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

  public static SupervisionEvent createSupervisionEvent() {
    return new SupervisionEventImpl(SupervisionConstants.SupervisionEntity.PROCESS, 50L,
        "P_TEST01", SupervisionConstants.SupervisionStatus.DOWN, new Timestamp(System.currentTimeMillis()), "");
  }
}
