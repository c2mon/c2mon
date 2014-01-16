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
