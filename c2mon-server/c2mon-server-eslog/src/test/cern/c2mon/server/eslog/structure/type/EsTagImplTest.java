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

import cern.c2mon.server.eslog.structure.types.EsTagImpl;
import cern.c2mon.server.eslog.structure.types.EsTagString;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

/**
 * @author Alban Marguet
 */
public class EsTagImplTest {
  private Gson gson = new GsonBuilder().create();
  private EsTagImpl esTagImpl;
  private String expectedJson;
  private long id = 1;
  private String name = "name";
  private String dataType = "string";
  private long sourceTimestamp = 123456;
  private long serverTimestamp = 123456;
  private long daqTimestamp = 123456;
  private int status = 0;
  private String quality = "{}";
  private Boolean valid = true;
  private Object value = "value";
  private Boolean valueBoolean;
  private String valueString = "value";
  private Number valueNumeric;
  private String valueDescription = "OK";
  private Map<String, String> metadata = new HashMap<>();

  @Before
  public void setup() {
    metadata.put("test1", "value1");
    metadata.put("test2", "value2");

    esTagImpl = new EsTagString();
    esTagImpl.setId(id);
    esTagImpl.setName(name);
    esTagImpl.setDataType(dataType);
    esTagImpl.setSourceTimestamp(sourceTimestamp);
    esTagImpl.setServerTimestamp(serverTimestamp);
    esTagImpl.setDaqTimestamp(daqTimestamp);
    esTagImpl.setStatus(status);
    esTagImpl.setQuality(quality);
    esTagImpl.setValid(valid);
    esTagImpl.setValue(value);
    esTagImpl.setValueBoolean(valueBoolean);
    esTagImpl.setValueString(valueString);
    esTagImpl.setValueNumeric(valueNumeric);
    esTagImpl.setValueDescription(valueDescription);
    esTagImpl.setMetadata(metadata);

    JsonObject element = gson.toJsonTree(new Object()).getAsJsonObject();
    element.addProperty("id", id);
    element.addProperty("name", name);
    element.addProperty("dataType", dataType);
    element.addProperty("sourceTimestamp", sourceTimestamp);
    element.addProperty("serverTimestamp", serverTimestamp);
    element.addProperty("daqTimestamp", daqTimestamp);
    element.addProperty("status", status);
    element.addProperty("quality", quality);
    element.addProperty("valid", valid);
    element.addProperty("valueBoolean", valueBoolean);
    element.addProperty("valueString", valueString);
    element.addProperty("valueNumeric", valueNumeric);
    element.addProperty("valueDescription", valueDescription);
    for (String key : metadata.keySet()) {
      element.addProperty(key, metadata.get(key));
    }
    expectedJson = gson.toJson(element);
  }

  @Test
  public void testJsonSerialization() {
    String json = esTagImpl.toString();
    try {
      assertEquals(expectedJson, esTagImpl.toString());
      assertEquals(esTagImpl, esTagImpl.getObject(expectedJson));
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("Should be able to serialize/deserialize JSON");
    }
  }
}