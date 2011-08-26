/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.shorttermlog.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.DataFormatException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.tim.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.tim.shared.common.supervision.SupervisionConstants.SupervisionStatus;

/**
 * Tests the iBatis mapper against the Oracle DB.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/shorttermlog/config/server-shorttermlog-test.xml"})
public class SupervisionMapperTest {

  /**
   * Test supervison event fields.
   */
  private static final Long ID = 10L;
  private static final SupervisionEntity ENTITY = SupervisionEntity.PROCESS;   
  private static final SupervisionStatus STATUS = SupervisionStatus.RUNNING;
  private static final java.sql.Timestamp DATE = new java.sql.Timestamp(System.currentTimeMillis());
  private static final String MESSAGE = null;
    
  /**
   * To test.
   */
  @Autowired
  private SupervisionMapper supervisionMapper;    
  
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
    supervisionMapper.testDelete(ID);
  }
  
  /**
   * Tests insertion completes successfully when fallback not
   * active (so no logtime set in object).
   */
  @Test
  public void testLogSupervision() {
    SupervisionEvent event = new SupervisionEventImpl(ENTITY, ID, STATUS, DATE, MESSAGE);
    supervisionMapper.logSupervisionEvent(event);
    
    //check event was properly saved
    List<SupervisionEvent> eventList = supervisionMapper.getEntitySupervision(ID);
    
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
