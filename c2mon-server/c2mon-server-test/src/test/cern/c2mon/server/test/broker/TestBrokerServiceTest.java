package cern.c2mon.server.test.broker;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.jms.Connection;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
    ApplicationContext context = new ClassPathXmlApplicationContext("cern/c2mon/server/test/broker/server-test-broker.xml");
    testBrokerService = context.getBean(TestBrokerService.class);
    assertNotNull(testBrokerService.getBroker());
    assertTrue(testBrokerService.getBroker().isStarted());
    Connection connection = testBrokerService.getConnectionFactory().createConnection();
    assertNotNull(connection);    
    testBrokerService.stopBroker();   
  }
  
}
