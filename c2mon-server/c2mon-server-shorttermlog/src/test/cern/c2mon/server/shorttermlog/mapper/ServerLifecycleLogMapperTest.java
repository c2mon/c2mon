package cern.c2mon.server.shorttermlog.mapper;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.shared.client.lifecycle.LifecycleEventType;
import cern.c2mon.shared.client.lifecycle.ServerLifecycleEvent;
import cern.c2mon.shared.client.lifecycle.ServerLifecycleMapper;

/**
 * Test of ServerLifecycleLogMapper writing/reading to/from Oracle DB.
 * 
 * @author Mark Brightwell
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "classpath:cern/c2mon/server/shorttermlog/config/server-shorttermlog-mapper-test.xml" })
public class ServerLifecycleLogMapperTest {

  private ServerLifecycleEvent testEvent = new ServerLifecycleEvent(new Timestamp(System.currentTimeMillis()), "testServer", LifecycleEventType.START);
  
  @Autowired
  private ServerLifecycleMapper serverLifecycleMapper;

  @Before
  public void beforeTest() {
    serverLifecycleMapper.deleteAllForServer(testEvent.getServerName());
  }

  @Test
  public void testLogSingleEvent() {
    serverLifecycleMapper.logEvent(testEvent);

    List<ServerLifecycleEvent> retrievedEvents = serverLifecycleMapper.getEventsForServer(testEvent.getServerName());

    assertNotNull(retrievedEvents);
    assertEquals(1, retrievedEvents.size());
    assertSameEvent(testEvent, retrievedEvents.get(0));
  }
  
  @Test
  public void testOrderedRetrievalOfEvents() {
    ServerLifecycleEvent secondEvent = new ServerLifecycleEvent(new Timestamp(System.currentTimeMillis() + 1000), "testServer", LifecycleEventType.STOP);
    serverLifecycleMapper.logEvent(testEvent);
    serverLifecycleMapper.logEvent(secondEvent);
    
    List<ServerLifecycleEvent> retrievedEvents = serverLifecycleMapper.getEventsForServer(testEvent.getServerName());
    assertNotNull(retrievedEvents);
    assertEquals(2, retrievedEvents.size());
    
    Iterator<ServerLifecycleEvent> it = retrievedEvents.iterator();
    assertSameEvent(testEvent, it.next());
    assertSameEvent(secondEvent, it.next());
  }
  
  @Test
  public void testGetForServer() {
    ServerLifecycleEvent otherServerEvent = new ServerLifecycleEvent(new Timestamp(System.currentTimeMillis() + 1000), "otherServer", LifecycleEventType.STOP);
    serverLifecycleMapper.logEvent(testEvent);
    serverLifecycleMapper.logEvent(otherServerEvent);
    
    List<ServerLifecycleEvent> retrievedEvents = serverLifecycleMapper.getEventsForServer(testEvent.getServerName());
    assertNotNull(retrievedEvents);
    assertEquals(1, retrievedEvents.size());
  }
  
  private boolean assertSameEvent(ServerLifecycleEvent expectedEvent, ServerLifecycleEvent actualEvent) {
    return expectedEvent.getEventTime().equals(actualEvent.getEventTime())
              && expectedEvent.getEventType().equals(actualEvent.getEventType())
              && expectedEvent.getServerName().equals(actualEvent.getServerName());
  }

}
