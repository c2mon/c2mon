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
package cern.c2mon.server.elasticsearch.structure.type;

import java.util.HashMap;
import java.util.Map;

import cern.c2mon.server.elasticsearch.alarm.AlarmDocument;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

/**
 * @author Alban Marguet
 */
@RunWith(JUnit4.class)
public class AlarmDocumentTest {
  private Gson gson = new GsonBuilder().create();
  private AlarmDocument alarmDocument;
  private String expectedJson;
  private long tagId = 1;
  private long alarmId = 2;

  private String faultFamily = "family";
  private String faultMember = "member";
  private int faultCode = 0;

  private boolean active = true;
  private double activeNumeric = 1;
  private String info = "info";

  private long serverTimestamp = 0;
  private Map<String, String> metadata = new HashMap<>();

  @Before
  public void setup() {
    metadata.put("test1", "value1");
    metadata.put("test2", "value2");

    alarmDocument = new AlarmDocument();
    alarmDocument.setId(alarmId);
    alarmDocument.setTagId(tagId);
    alarmDocument.setFaultFamily(faultFamily);
    alarmDocument.setFaultMember(faultMember);
    alarmDocument.setFaultCode(faultCode);
    alarmDocument.setActive(active);
    alarmDocument.setActiveNumeric(activeNumeric);
    alarmDocument.setInfo(info);
    alarmDocument.setTimestamp(serverTimestamp);
    alarmDocument.getMetadata().putAll(metadata);

    expectedJson = gson.toJson(alarmDocument);
  }

  @Test
  public void testJsonSerialization() {
    try {
      assertEquals(expectedJson, alarmDocument.toString());
      assertEquals(alarmDocument, alarmDocument.getObject(expectedJson));
    } catch(Exception e) {
      fail("Should be able to serialize/deserialize JSON");
    }
  }
}
