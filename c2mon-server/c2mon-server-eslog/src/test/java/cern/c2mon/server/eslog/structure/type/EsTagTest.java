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
package cern.c2mon.server.eslog.structure.type;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import cern.c2mon.server.eslog.structure.types.tag.EsTag;
import cern.c2mon.server.eslog.structure.types.tag.TagQualityAnalysis;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

/**
 * @author Alban Marguet
 */
@RunWith(JUnit4.class)
public class EsTagTest {
  private Gson gson = new GsonBuilder().create();
  private EsTag esTag;
  private String expectedJson;
  private long id = 1;
  private String name = "name";
  private String dataType = "String";
  private long timestamp = 123456;
  private long serverTimestamp = 123456;
  private long daqTimestamp = 123456;

  private TagQualityAnalysis quality = new TagQualityAnalysis();
  private Object value = "rawValue";
  private Boolean valueBoolean;
  private String valueString = "rawValue";
  private Number valueNumeric;
  private String valueDescription = "OK";
  private Map<String, String> metadata = new HashMap<>();

  @Before
  public void setup() {
    esTag = new EsTag(id, dataType);
    esTag.setName(name);

    esTag.setTimestamp(timestamp);
    esTag.getC2mon().setServerTimestamp(serverTimestamp);
    esTag.getC2mon().setDaqTimestamp(daqTimestamp);

    quality.setStatus(0);
    quality.setValid(true);
    esTag.setQuality(quality);

    esTag.setRawValue(value);
    esTag.setValueBoolean(valueBoolean);
    esTag.setValueString(valueString);
    esTag.setValue(valueNumeric);
    esTag.setValueDescription(valueDescription);

    metadata.put("test1", "value1");
    metadata.put("test2", "value2");
    esTag.getMetadata().putAll(metadata);

    expectedJson = gson.toJson(esTag);
  }

  @Test
  public void testJsonSerialization() {
    try {
      assertEquals(expectedJson, esTag.toString());
      assertEquals(esTag, esTag.getObject(expectedJson));
    } catch(Exception e) {
      e.printStackTrace();
      fail("Should be able to serialize/deserialize JSON");
    }
  }
}