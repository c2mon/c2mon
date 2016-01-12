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
package cern.c2mon.server.daqcommunication.out;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.test.broker.TestBrokerService;

/**
 * Simple test checking that the JMS connection starts up correctly.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/daqcommunication/out/config/server-daqcommunication-out-connection-test.xml"})
public class ConnectionTest {
 
  private static TestBrokerService testBrokerService = new TestBrokerService();
  
  @BeforeClass
  public static void startBroker() throws Exception {
    testBrokerService.createAndStartBroker();
  }
  
  @AfterClass
  public static void stopBroker() throws Exception {
    testBrokerService.stopBroker();
  }
  
  /**
   * Tests the connection starts up.
   */
  @Test
  public void testConnectionStart(){
    //do nothing
  }
}
