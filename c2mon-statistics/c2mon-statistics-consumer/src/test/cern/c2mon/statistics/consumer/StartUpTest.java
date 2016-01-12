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
package cern.c2mon.statistics.consumer;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Tests the application starts up, connecting to in memory broker.
 * 
 * @author Mark Brightwell
 *
 */
public class StartUpTest {

  private static BrokerService broker =  null;
  private static ActiveMQConnectionFactory c = null;
  
  @BeforeClass
  public static void startBroker() throws Exception {
        broker = new BrokerService();
        broker.setPersistent(false);
        broker.setUseShutdownHook(false);
        broker.setUseJmx(false);
        broker.start();
        
        c = new ActiveMQConnectionFactory();
        c.setObjectMessageSerializationDefered(true);
        c.setBrokerURL("vm://localhost");
        c.setCopyMessageOnSend(false);
        
        
  }

  @AfterClass
  public static void stopBroker() {
        try {
              broker.stop();
        } catch (Exception e) {
              e.printStackTrace();
        }
  }
  
  @Test
  public void testStartUp() {
    AbstractApplicationContext context = new ClassPathXmlApplicationContext("resources/consumer-service.xml");
  }

    
}
