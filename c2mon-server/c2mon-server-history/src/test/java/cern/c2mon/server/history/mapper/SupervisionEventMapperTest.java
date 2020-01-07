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
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the iBatis mapper against the Oracle DB.
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
    DaqModule.class,
    CommandModule.class,
    HistoryModule.class
})
public class SupervisionEventMapperTest {

  /**
   * Test supervison event fields.
   */
  private static final Long ID = 10L;
  private static final String NAME = "P_TEST";
  private static final SupervisionEntity ENTITY = SupervisionEntity.PROCESS;
  private static final SupervisionStatus STATUS = SupervisionStatus.RUNNING;
  private static final java.sql.Timestamp DATE = new java.sql.Timestamp(System.currentTimeMillis());
  private static final String MESSAGE = null;

  /**
   * To test.
   */
  @Autowired
  private SupervisionEventMapper supervisionEventMapper;

  /**
   * Removes test values from previous tests in case clean up failed.
   */
  @Before
  public void beforeTest() {
    removeTestData();
  }

  /**
   * Removes test values after test.
   */
  @After
  public void afterTest() {
    removeTestData();
  }

  /**
   * Removes test data (all logs for the entity with the given id).
   */
  private void removeTestData() {
    supervisionEventMapper.testDelete(ID);
  }

  /**
   * Tests insertion completes successfully when fallback not
   * active (so no logtime set in object).
   */
  @Test
  public void testLogSupervision() {
    SupervisionEvent event = new SupervisionEventImpl(ENTITY, ID, NAME, STATUS, DATE, MESSAGE);
    supervisionEventMapper.logSupervisionEvent(event);

    //check event was properly saved
    List<SupervisionEvent> eventList = supervisionEventMapper.getEntitySupervision(ID);

    assertNotNull(eventList);
    assertEquals(1, eventList.size());

    SupervisionEvent retrievedEvent = eventList.get(0);
    assertEquals(event.getEntityId(), retrievedEvent.getEntityId());
    assertEquals(event.getEntity(), retrievedEvent.getEntity());
    assertEquals(event.getStatus(), retrievedEvent.getStatus());

    //check time is logged in UTC format to DB
    int offset = TimeZone.getDefault().getOffset(event.getEventTime().getTime());
    assertEquals(event.getEventTime().getTime(), retrievedEvent.getEventTime().getTime() + offset);

    assertEquals(event.getMessage(), retrievedEvent.getMessage());
  }

}
