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
package cern.c2mon.client.ext.history.dbaccess.spring;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.ext.history.dbaccess.HistorySessionFactory;

/**
 * Tests for the {@link HistorySessionFactoryTest} class
 * 
 * @author ekoufaki
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    locations = 
  {
        "classpath:cern/c2mon/client/ext/history/c2mon-historymanager-test.xml"
        ,
        "classpath:cern/c2mon/client/ext/history/dbaccess/spring/config/spring-history-test.xml" 
  })
public class HistorySessionFactoryTest {
  
  @Autowired
  HistorySessionFactory f;

  @Test
  public void testSessionFactory() {

    assertTrue(f.getHistoryMapper() != null);
    assertTrue(f.getSavedHistoryMapper() != null);
    assertTrue(f.getSavedHistoryEventsMapper() != null);
  }
}
