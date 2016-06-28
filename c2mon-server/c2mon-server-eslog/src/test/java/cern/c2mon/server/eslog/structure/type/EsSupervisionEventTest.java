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

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.server.eslog.structure.types.EsSupervisionEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

/**
 * @author Alban Marguet
 */
@RunWith(JUnit4.class)
public class EsSupervisionEventTest {
  private Gson gson = new GsonBuilder().create();
  private EsSupervisionEvent esSupervisionEvent;
  private String expectedJson;
  private long id = 1;
  private String entityName = "entity";
  private String message = "message";
  private String statusName = "status";
  private long eventTime = 0;

  @Before
  public void setup() {
    esSupervisionEvent = new EsSupervisionEvent();
    esSupervisionEvent.setEntityId(id);
    esSupervisionEvent.setEntityName(entityName);
    esSupervisionEvent.setMessage(message);
    esSupervisionEvent.setStatusName(statusName);
    JsonObject element = gson.toJsonTree(new Object()).getAsJsonObject();
    element.addProperty("entityId", id);
    element.addProperty("message", message);
    element.addProperty("entityName", entityName);
    element.addProperty("statusName", statusName);
    element.addProperty("eventTime", eventTime);
    expectedJson = gson.toJson(element);
  }

  @Test
  public void testJsonSerialization() {
    String json = esSupervisionEvent.toString();
    try {
      assertEquals(expectedJson, esSupervisionEvent.toString());
      assertEquals(esSupervisionEvent, esSupervisionEvent.getObject(expectedJson));
    } catch(Exception e) {
      fail("Should be able to serialize/deserialize JSON");
    }
  }

  @Test
  public void testGetObject() {
    String line = "{\"entityId\":1,\"message\":\"message\",\"entityName\":\"entity\",\"statusName\":\"status\",\"eventTime\":0}";
    IFallback result = esSupervisionEvent.getObject(line);
    assertTrue(result instanceof EsSupervisionEvent);
    Assert.assertEquals(line, result.toString());
  }
}