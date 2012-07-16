package cern.c2mon.client.jms;

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
    c.setBrokerURL("vm://localhost");
    c.setCopyMessageOnSend(false);    

    //reset the system properties so the connection is made to this broker
    System.setProperty("c2mon.client.jms.url", "vm://localhost");
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
