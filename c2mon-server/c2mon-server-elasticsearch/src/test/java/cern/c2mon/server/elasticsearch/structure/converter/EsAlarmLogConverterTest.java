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
package cern.c2mon.server.elasticsearch.structure.converter;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.elasticsearch.structure.types.EsAlarm;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.common.metadata.Metadata;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

/**
 * Insure that EsAlarmLogConverter converts well Alarm to EsAlarm.
 * @author Alban Marguet
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class EsAlarmLogConverterTest {
  @InjectMocks
  private EsAlarmLogConverter esAlarmLogConverter;
  private Alarm alarm;
  private EsAlarm esAlarm;
  private String[] arrayString = new String[]{};

  @Test
  public void testNullConvertsToNull() {
    esAlarm = esAlarmLogConverter.convert(alarm);
    assertNull(esAlarm);
  }

  @Test
  public void testDataIsPreserved() {
    alarm = CacheObjectCreation.createTestAlarm1();
    esAlarm = esAlarmLogConverter.convert(alarm);

    callTests(esAlarm, alarm);
  }

  @Test
  public void testMetadata() {
    alarm = initAlarmWithMetadata();

    esAlarm = esAlarmLogConverter.convert(alarm);
    callTests(esAlarm, alarm);
    log.debug(arrayString.toString());
    assertEquals(arrayString.toString(), esAlarm.getMetadata().get("array"));
  }

  private void callTests(EsAlarm esAlarm, Alarm alarm) {
    log.debug(esAlarm.toString());
    assertEquals(alarm.getTagId().longValue(), esAlarm.getTagId());
    assertEquals(alarm.getId().longValue(), esAlarm.getIdAsLong());
    assertEquals(alarm.getFaultFamily(), esAlarm.getFaultFamily());
    assertEquals(alarm.getFaultMember(), esAlarm.getFaultMember());
    assertEquals(alarm.getFaultCode(), esAlarm.getFaultCode());
    assertEquals(alarm.isActive(), esAlarm.isActive());
    assertEquals(alarm.getInfo(), esAlarm.getInfo());
    assertEquals(alarm.getTimestamp().getTime(), esAlarm.getTimestamp());
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
    alarm.setTimestamp(new Timestamp(System.currentTimeMillis()));

    return alarm;
  }

}
