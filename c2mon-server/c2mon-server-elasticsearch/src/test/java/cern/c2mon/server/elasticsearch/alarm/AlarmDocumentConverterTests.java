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
package cern.c2mon.server.elasticsearch.alarm;

import java.sql.Timestamp;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.internal.util.reflection.Whitebox;

import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.elasticsearch.util.EntityUtils;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 */
@Slf4j
@RunWith(JUnit4.class)
public class AlarmDocumentConverterTests {

  private static final String TIMESTAMP_PROPERTY = "timestamp";

  private AlarmValueDocumentConverter converter = new AlarmValueDocumentConverter();

  @Test
  public void toAndFromJson() throws DataFallbackException {
    Alarm alarm = EntityUtils.createAlarm();
    AlarmDocument document = converter.convert(alarm);

    // Serialize
    String json = document.toString();

    // Deserialize
    document = (AlarmDocument) document.getObject(json);

    assertEquals(alarm.getId().intValue(), document.get("id"));
    assertEquals(alarm.getTagId().intValue(), document.get("tagId"));
    assertEquals(alarm.getFaultFamily(), document.get("faultFamily"));
    assertEquals(alarm.getFaultMember(), document.get("faultMember"));
    assertEquals(alarm.getFaultCode(), document.get("faultCode"));
    assertEquals(alarm.isActive(), document.get("active"));
    assertEquals(alarm.getInfo(), document.get("info"));
    assertEquals(alarm.getTimestamp().getTime(), document.get(TIMESTAMP_PROPERTY));

    Map<String, Object> metadata = (Map<String, Object>) document.get("metadata");
    assertEquals(alarm.getMetadata().getMetadata().get("building"), metadata.get("building"));
    assertEquals(alarm.getMetadata().getMetadata().get("array"), metadata.get("array"));
    assertEquals(alarm.getMetadata().getMetadata().get("responsiblePerson"), metadata.get("responsiblePerson"));
  }

  /**
   * Timestamp should not be 0 but in case it is
   * it should still be a Long not an Integer.
   */
  @Test
  public void convertZeroTimestamp() throws DataFallbackException {
    Alarm alarm = EntityUtils.createAlarm();
    Whitebox.setInternalState(alarm, TIMESTAMP_PROPERTY, new Timestamp(0));
    AlarmDocument document = converter.convert(alarm);

    // Serialize
    String json = document.toString();

    // Deserialize
    document = (AlarmDocument) document.getObject(json);
    assertEquals(Long.class, document.get(TIMESTAMP_PROPERTY).getClass());
    assertEquals(0L, document.get(TIMESTAMP_PROPERTY));
  }
}
