package cern.c2mon.shared.client.supervision;

import java.sql.Timestamp;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

public class SupervisionEventImplTest {

  SupervisionEventImpl event;
  
  @Before
  public void init() {
    event = 
      new SupervisionEventImpl(SupervisionEntity.EQUIPMENT,
                               Long.valueOf(1324L),
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
    assertEquals(event.getEventTime(), receivedEvent.getEventTime());
    assertEquals(event.getMessage(), receivedEvent.getMessage());
    assertEquals(event.getStatus(), receivedEvent.getStatus());
  }
}
