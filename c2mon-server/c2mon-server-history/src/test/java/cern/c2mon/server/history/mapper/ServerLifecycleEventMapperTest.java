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
package cern.c2mon.server.history.mapper;

import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.command.config.CommandModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.daq.config.DaqModule;
import cern.c2mon.server.history.config.HistoryModule;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.shared.client.lifecycle.LifecycleEventType;
import cern.c2mon.shared.client.lifecycle.ServerLifecycleEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test of ServerLifecycleLogMapper writing/reading to/from Oracle DB.
 *
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheConfigModuleRef.class,
    CacheDbAccessModule.class,
    CacheLoadingModuleRef.class,
    SupervisionModule.class,
    CommandModule.class,
    DaqModule.class,
    HistoryModule.class
})
public class ServerLifecycleEventMapperTest {

  private ServerLifecycleEvent testEvent = new ServerLifecycleEvent(new Timestamp(System.currentTimeMillis()), "testServer", LifecycleEventType.START);

  @Autowired
  private ServerLifecycleEventMapper serverLifecycleEventMapper;

  @Before
  public void beforeTest() {
    serverLifecycleEventMapper.deleteAllForServer(testEvent.getServerName());
  }

  @Test
  public void testLogSingleEvent() {
    serverLifecycleEventMapper.logEvent(testEvent);

    List<ServerLifecycleEvent> retrievedEvents = serverLifecycleEventMapper.getEventsForServer(testEvent.getServerName());

    assertNotNull(retrievedEvents);
    assertEquals(1, retrievedEvents.size());
    assertSameEvent(testEvent, retrievedEvents.get(0));
  }

  @Test
  public void testOrderedRetrievalOfEvents() {
    ServerLifecycleEvent secondEvent = new ServerLifecycleEvent(new Timestamp(System.currentTimeMillis() + 1000), "testServer", LifecycleEventType.STOP);
    serverLifecycleEventMapper.logEvent(testEvent);
    serverLifecycleEventMapper.logEvent(secondEvent);

    List<ServerLifecycleEvent> retrievedEvents = serverLifecycleEventMapper.getEventsForServer(testEvent.getServerName());
    assertNotNull(retrievedEvents);
    assertEquals(2, retrievedEvents.size());

    Iterator<ServerLifecycleEvent> it = retrievedEvents.iterator();
    assertSameEvent(testEvent, it.next());
    assertSameEvent(secondEvent, it.next());
  }

  @Test
  public void testGetForServer() {
    ServerLifecycleEvent otherServerEvent = new ServerLifecycleEvent(new Timestamp(System.currentTimeMillis() + 1000), "otherServer", LifecycleEventType.STOP);
    serverLifecycleEventMapper.logEvent(testEvent);
    serverLifecycleEventMapper.logEvent(otherServerEvent);

    List<ServerLifecycleEvent> retrievedEvents = serverLifecycleEventMapper.getEventsForServer(testEvent.getServerName());
    assertNotNull(retrievedEvents);
    assertEquals(1, retrievedEvents.size());
  }

  private boolean assertSameEvent(ServerLifecycleEvent expectedEvent, ServerLifecycleEvent actualEvent) {
    return expectedEvent.getEventTime().equals(actualEvent.getEventTime())
              && expectedEvent.getEventType().equals(actualEvent.getEventType())
              && expectedEvent.getServerName().equals(actualEvent.getServerName());
  }

}
