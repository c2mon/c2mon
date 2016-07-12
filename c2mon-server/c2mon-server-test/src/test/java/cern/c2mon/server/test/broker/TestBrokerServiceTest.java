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
package cern.c2mon.server.test.broker;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.jms.Connection;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Ignore
public class TestBrokerServiceTest {

  private TestBrokerService testBrokerService;
  
  @Before
  public void setUp () {
    testBrokerService = new TestBrokerService();
  }
  
  @Test
  public void testBrokerStartAndStop() throws Exception {
    testBrokerService.createAndStartBroker();
    Connection connection = testBrokerService.getConnectionFactory().createConnection();
    assertNotNull(connection);    
    testBrokerService.stopBroker();
  }
  
  @Test
  public void testAddTransportConnector() throws Exception {    
    testBrokerService.setExternalAccessUrl("tcp://localhost:61620");
    testBrokerService.createAndStartBroker();  
    Connection connection = testBrokerService.getConnectionFactory().createConnection();
    assertNotNull(connection);    
    testBrokerService.stopBroker();
  }
  
  @Test
  public void testBrokerStartFromXml() throws Exception {
    ApplicationContext context = new ClassPathXmlApplicationContext("server-test-broker.xml");
    testBrokerService = context.getBean(TestBrokerService.class);
    assertNotNull(testBrokerService.getBroker());
    assertTrue(testBrokerService.getBroker().isStarted());
    Connection connection = testBrokerService.getConnectionFactory().createConnection();
    assertNotNull(connection);    
    testBrokerService.stopBroker();   
  }
  
}
