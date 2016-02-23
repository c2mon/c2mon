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

import java.net.URI;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;

/**
 * Manages an in-memory broker. Singleton Spring bean.
 * 
 * @author Mark Brightwell
 *
 * @deprecated
 */
public class TestBrokerService {

  private BrokerService broker;
  private ActiveMQConnectionFactory c;
  private String externalAccessUrl;
  
  /**
   * Creates and starts the broker. Also sets the following system
   * properties for use in tests:
   * 
   * <p>jms.broker.url</br>
   * jms.client.user</br>
   * jms.client.password
   * 
   * <p>If externalAccessUrl is set, the broker will be visible
   * outside the JVM on this URL (internal on vm://localhost).
   * 
   * <p>Only returns once broker is started.
   * 
   * @throws Exception if problem starting the broker
   */
  public void createAndStartBroker() throws Exception {
    String brokerUrl = "failover:(vm://localhost?create=false)?timeout=100&useExponentialBackOff=false&initialReconnectDelay=100";
    broker = new BrokerService();
    broker.setPersistent(false);
    broker.setUseShutdownHook(false);
    broker.setUseJmx(true); 
    
    if (externalAccessUrl != null){
      TransportConnector connector =new TransportConnector();
      connector.setUri(new URI(externalAccessUrl));
      broker.addConnector(connector);
    }
    
    broker.start();    

    //only create now as o.w. 
    c = new ActiveMQConnectionFactory();
    c.setObjectMessageSerializationDefered(true);
    c.setBrokerURL(brokerUrl);
    c.setCopyMessageOnSend(false);    

    //reset url property: only useful when loaded prior to Spring context loading via Junit!
    System.setProperty("jms.broker.url", brokerUrl);
    System.setProperty("jms.broker.client.url", brokerUrl);
    System.setProperty("jms.client.user", "");
    System.setProperty("jms.client.password", "");
    
    broker.waitUntilStarted();
  }
  
  /**
   * Stops the test broker.
   * @throws Exception if problem stopping the broker
   */
  public void stopBroker() throws Exception {
    broker.stop();
    broker.waitUntilStopped();
  }
  
  /**
   * Returns a unique JMS ConnectionFactory to this broker.
   * @return
   */
  public ConnectionFactory getConnectionFactory() {    
    return c;
  }
  
  /**
   * @return Returns the broker service
   */
  public BrokerService getBroker() {
    return broker;
  }
  
  /**
   * Import the associated XML to start this bean and the broker.
   * @throws Exception
   */
  @PostConstruct
  public void automaticBrokerStart() throws Exception {    
    createAndStartBroker();
  }
  
  /**
   * Import the associated XML to start this bean.
   * @throws Exception
   */
  @PreDestroy
  public void automaticsBrokerStop() throws Exception {
    stopBroker();
  }

  /**
   * Set this if require access to broker from outside JVM, 
   * e.g. "tcp://localhost:61620"
   * @param externalAccessUrl
   */
  public void setExternalAccessUrl(String externalAccessUrl) {
    this.externalAccessUrl = externalAccessUrl;
  }

  /**
   * @return URL for out-of-JVM access
   */
  public String getExternalAccessUrl() {
    return externalAccessUrl;
  }
  
}
