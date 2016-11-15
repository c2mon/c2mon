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
package cern.c2mon.client.core.jms;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

/**
 * Provides static methods for starting and stopping an in-memory
 * test ActiveMQ JMS broker, for testing the C2MON Client API.
 *
 * @author Mark Brightwell
 *
 */
public class TestBrokerService {

  private static BrokerService broker;
  private static ActiveMQConnectionFactory c;

  /**
   * Creates and starts the broker. Also sets the following system
   * properties for use in tests:
   *
   * jms.broker.url
   * jms.client.user
   * jms.client.password
   *
   * @throws Exception if problem starting the broker
   */
  public static void createAndStartBroker() throws Exception {
    broker = new BrokerService();
    broker.setPersistent(false);
    broker.setUseShutdownHook(false);
    broker.setUseJmx(false);
    broker.start();

    c = new ActiveMQConnectionFactory();
    c.setObjectMessageSerializationDefered(true);
    c.setBrokerURL("vm:(broker:(tcp://0.0.0.0:61616)?persistent=false)");
    c.setCopyMessageOnSend(false);

    //reset the system properties so the connection is made to this broker
    System.setProperty("c2mon.client.jms.url", "vm://localhost:61616");
    System.setProperty("c2mon.client.jms.user", "");
    System.setProperty("c2mon.client.jms.password", "");
  }

  /**
   * Stops the test broker.
   * @throws Exception if problem stopping the broker
   */
  public static void stopBroker() throws Exception {
    broker.stop();
  }

  /**
   * Returns a unique JMS ConnectionFactory to this broker.
   * @return
   */
  public static ConnectionFactory getConnectionFactory() {
    return c;
  }
}
