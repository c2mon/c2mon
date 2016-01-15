/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.eslog.structure.queries;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.eslog.structure.converter.AlarmESLogConverter;
import cern.c2mon.server.eslog.structure.types.AlarmES;
import cern.c2mon.server.test.CacheObjectCreation;
import org.elasticsearch.client.Client;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Alban Marguet
 */
public class AlarmESQueryTest {
  private long tagId;
  private long alarmId;
  private String faultFamily;
  private String faultMember;
  private int faultCode;
  private boolean active;
  private int priority;
  private String info;
  private long serverTimestamp;
  private String timeZone;
  private Map<String, Object> json;
  AlarmESQuery query;
  Client client;
  AlarmESLogConverter alarmESLogConverter;
  Alarm alarm;
  AlarmES alarmES;

  @Before
  public void setup() {
    alarm = CacheObjectCreation.createTestAlarm1();
    alarmESLogConverter = new AlarmESLogConverter();
    alarmES = alarmESLogConverter.convertAlarmToAlarmES(alarm);
    tagId = alarm.getTagId();
    alarmId = alarm.getId();
    faultFamily = alarm.getFaultFamily();
    faultMember = alarm.getFaultMember();
    faultCode = alarm.getFaultCode();
    active = alarm.isActive();
    info = alarm.getInfo();
    serverTimestamp = alarm.getTimestamp().getTime();

    json = new HashMap<>();
    json.put("tagId", tagId);
    json.put("alarmId", alarmId);
    json.put("faultFamily", faultFamily);
    json.put("faultMember", faultMember);
    json.put("faultCode", faultCode);
    json.put("active", active);
    json.put("priority", priority);
    json.put("info", info);
    json.put("serverTimeStamp", serverTimestamp);
    json.put("timeZone", timeZone);

    query = new AlarmESQuery(client, alarmES);
  }

  @Test
  public void testCorrectOutput() {
    assertEquals(tagId, query.getTagId());
    assertEquals(alarmId, query.getAlarmId());
    assertEquals(faultFamily, query.getFaultFamily());
    assertEquals(faultMember, query.getFaultMember());
    assertEquals(faultCode, query.getFaultCode());
    assertEquals(active, query.isActive());
    assertEquals(priority, query.getPriority());
    assertEquals(info, query.getInfo());
    assertEquals(serverTimestamp, query.getServerTimestamp());
    assertEquals(timeZone, query.getTimeZone());

    assertEquals(json, query.getJson());
  }
}