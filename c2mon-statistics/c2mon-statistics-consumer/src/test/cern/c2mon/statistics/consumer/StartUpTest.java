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
