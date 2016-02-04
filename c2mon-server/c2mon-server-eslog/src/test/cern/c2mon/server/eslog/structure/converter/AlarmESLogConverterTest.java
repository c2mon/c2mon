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
package cern.c2mon.server.eslog.structure.converter;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.eslog.structure.types.AlarmES;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.common.metadata.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

/**
 * Insure that AlarmESLogConverter converts well Alarm to AlarmES.
 * @author Alban Marguet
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class AlarmESLogConverterTest {
  @InjectMocks
  private AlarmESLogConverter alarmESLogConverter;
  private Alarm alarm;
  private AlarmES alarmES;
  private String[] arrayString = new String[]{};

  @Test
  public void testNullConvertsToNull() {
    alarmES = alarmESLogConverter.convertAlarmToAlarmES(alarm);
    assertNull(alarmES);
  }

  @Test
  public void testDataIsPreserved() {
    alarm = CacheObjectCreation.createTestAlarm1();
    alarmES = alarmESLogConverter.convertAlarmToAlarmES(alarm);

    callTests(alarmES, alarm);
  }

  @Test
  public void testMetadata() {
    alarm = initAlarmWithMetadata();

    alarmES = alarmESLogConverter.convertAlarmToAlarmES(alarm);
    callTests(alarmES, alarm);
    log.debug(arrayString.toString());
    assertEquals(arrayString.toString(), alarmES.getMetadata().get("array"));
  }

  private void callTests(AlarmES alarmES, Alarm  alarm) {
    log.debug(alarmES.toString());
    assertEquals(alarm.getTagId().longValue(), alarmES.getTagId());
    assertEquals(alarm.getId().longValue(), alarmES.getAlarmId());
    assertEquals(alarm.getFaultFamily(), alarmES.getFaultFamily());
    assertEquals(alarm.getFaultMember(), alarmES.getFaultMember());
    assertEquals(alarm.getFaultCode(), alarmES.getFaultCode());
    assertEquals(alarm.isActive(), alarmES.isActive());
    assertEquals(alarm.getInfo(), alarmES.getInfo());
    assertEquals(alarm.getTimestamp().getTime(), alarmES.getServerTimestamp());
  }

  private Alarm initAlarmWithMetadata() {
    AlarmCacheObject alarm = new AlarmCacheObject();
    Map<String, Object> map = new HashMap<>();
    map.put("building", "1");
    map.put("array", arrayString.toString());
    map.put("responsible person", "coucou");
    Metadata metadata = new Metadata();
    metadata.setMetadata(map);
    alarm.setId(1L);
    alarm.setDataTagId(2L);
    alarm.setMetadata(metadata);

    return alarm;
  }
}