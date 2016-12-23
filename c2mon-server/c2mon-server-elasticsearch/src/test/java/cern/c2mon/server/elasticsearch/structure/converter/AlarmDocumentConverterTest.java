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

import cern.c2mon.server.elasticsearch.alarm.AlarmDocumentConverter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.elasticsearch.alarm.AlarmDocument;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.common.metadata.Metadata;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

/**
 * Insure that EsAlarmLogConverter converts well Alarm to AlarmDocument.
 * @author Alban Marguet
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class AlarmDocumentConverterTest {
  @InjectMocks
  private AlarmDocumentConverter alarmDocumentConverter;
  private Alarm alarm;
  private AlarmDocument alarmDocument;
  private String[] arrayString = new String[]{};

  @Test
  public void testNullConvertsToNull() {
    alarmDocument = alarmDocumentConverter.convert(alarm);
    assertNull(alarmDocument);
  }

  @Test
  public void testDataIsPreserved() {
    alarm = CacheObjectCreation.createTestAlarm1();
    alarmDocument = alarmDocumentConverter.convert(alarm);

    callTests(alarmDocument, alarm);
  }

  @Test
  public void testMetadata() {
    alarm = initAlarmWithMetadata();

    alarmDocument = alarmDocumentConverter.convert(alarm);
    callTests(alarmDocument, alarm);
    log.debug(arrayString.toString());
    assertEquals(arrayString.toString(), alarmDocument.getMetadata().get("array"));
  }

  private void callTests(AlarmDocument alarmDocument, Alarm alarm) {
    log.debug(alarmDocument.toString());
    assertEquals(alarm.getTagId().longValue(), alarmDocument.getTagId());
    assertEquals(alarm.getId().longValue(), alarmDocument.getIdAsLong());
    assertEquals(alarm.getFaultFamily(), alarmDocument.getFaultFamily());
    assertEquals(alarm.getFaultMember(), alarmDocument.getFaultMember());
    assertEquals(alarm.getFaultCode(), alarmDocument.getFaultCode());
    assertEquals(alarm.isActive(), alarmDocument.isActive());
    assertEquals(alarm.getInfo(), alarmDocument.getInfo());
    assertEquals(alarm.getTimestamp().getTime(), alarmDocument.getTimestamp());
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
