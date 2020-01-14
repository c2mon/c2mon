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
package cern.c2mon.shared.client.supervision;

import cern.c2mon.shared.common.supervision.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;

import static junit.framework.TestCase.assertEquals;

public class SupervisionEventImplTest {

  SupervisionEventImpl event;

  @Before
  public void init() {
    event =
      new SupervisionEventImpl(SupervisionEntity.EQUIPMENT,
                               Long.valueOf(1324L),
                               "E_TEST",
                               SupervisionStatus.RUNNING,
                               new Timestamp(1343813680995L),
                               "Supervision Test event");
  }

  @Test
  public void testJsonMessage() {
    String jsonMsg = event.toJson();

    SupervisionEvent receivedEvent = SupervisionEventImpl.fromJson(jsonMsg);
    assertEquals(event.getEntity(), receivedEvent.getEntity());
    assertEquals(event.getEntityId(), receivedEvent.getEntityId());
    assertEquals(event.getName(), receivedEvent.getName());
    assertEquals(event.getEventTime(), receivedEvent.getEventTime());
    assertEquals(event.getMessage(), receivedEvent.getMessage());
    assertEquals(event.getStatus(), receivedEvent.getStatus());
  }

  /**
   * Can a new Json lib be introduced on the clients and still decode current
   * server messages?
   */
  @Test
  public void testBackwardsCompatibility() {
    //current expected encoding
    String encoded = "{\"entity\":\"EQUIPMENT\",\"entityId\":1324,\"status\":\"RUNNING\",\"eventTime\":1343813680995,\"message\":\"Supervision Test event\"}";

    SupervisionEvent receivedEvent = SupervisionEventImpl.fromJson(encoded);
    assertEquals(event.getEntity(), receivedEvent.getEntity());
    assertEquals(event.getEntityId(), receivedEvent.getEntityId());
    assertEquals(event.getEventTime(), receivedEvent.getEventTime());
    assertEquals(event.getMessage(), receivedEvent.getMessage());
    assertEquals(event.getStatus(), receivedEvent.getStatus());
  }

  @Test
  public void testJsonDeserializationWithUnkownField() {
    StringBuffer jsonMsgBuffer = new StringBuffer(event.toJson());
    jsonMsgBuffer.deleteCharAt(jsonMsgBuffer.length() - 1);
    jsonMsgBuffer.append(",\"unknownField\":\"Does not exist!\"}");

    SupervisionEvent receivedEvent = SupervisionEventImpl.fromJson(jsonMsgBuffer.toString());
    assertEquals(event.getEntity(), receivedEvent.getEntity());
    assertEquals(event.getEntityId(), receivedEvent.getEntityId());
    assertEquals(event.getName(), receivedEvent.getName());
    assertEquals(event.getEventTime(), receivedEvent.getEventTime());
    assertEquals(event.getMessage(), receivedEvent.getMessage());
    assertEquals(event.getStatus(), receivedEvent.getStatus());
  }
}
