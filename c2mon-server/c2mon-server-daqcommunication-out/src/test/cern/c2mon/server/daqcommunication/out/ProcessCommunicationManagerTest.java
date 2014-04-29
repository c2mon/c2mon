package cern.c2mon.server.daqcommunication.out;

import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.activemq.command.ActiveMQQueue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.test.broker.TestBrokerService;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import cern.c2mon.shared.daq.config.ConfigurationDOMFactory;

/**
 * Integration test of ProcessCommunicationManager with rest of core.
 * 
 * @author Mark Brightwell
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "classpath:cern/c2mon/server/daqcommunication/out/config/server-daqcommunication-out-manager-test.xml" })
public class ProcessCommunicationManagerTest {
  
  /**
   * To test.
   */
  @Autowired
  private ProcessCommunicationManager processCommunicationManager;

  @Autowired  
  private ProcessCache processCache; 
  
  private static TestBrokerService testBrokerService = new TestBrokerService();
  
  /**
   * Starts in-memory broker.
   */
  @BeforeClass
  public static void init() throws Exception {
    testBrokerService.createAndStartBroker();
  }

  @AfterClass
  public static void stopBroker() throws Exception {
    testBrokerService.stopBroker();
  }

  /**
   * Tests request is sent and response is processed. Connects to in-memory
   * broker.
   * @throws NoSimpleValueParseException 
   * @throws NoSuchFieldException 
   * @throws TransformerException 
   * @throws InstantiationException 
   * @throws IllegalAccessException 
   * @throws ParserConfigurationException 
   */
  @Test
  public void testConfigurationRequest() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException {    
    //fake DAQ responding to request
    final JmsTemplate daqTemplate = new JmsTemplate(testBrokerService.getConnectionFactory()); 
    new Thread(new Runnable() {
      
      @Override
      public void run() {
        try {
          daqTemplate.execute(new SessionCallback<Object>() {
            String reportString = new ConfigurationDOMFactory().createConfigurationChangeEventReportXMLString(new ConfigurationChangeEventReport());
            @Override
            public Object doInJms(Session session) throws JMSException {
              MessageConsumer consumer = session.createConsumer(new ActiveMQQueue(processCache.get(50L).getJmsDaqCommandQueue()));
              Message incomingMessage = consumer.receive(100000);
              MessageProducer messageProducer = session.createProducer(incomingMessage.getJMSReplyTo());      
              TextMessage replyMessage = session.createTextMessage();
              replyMessage.setText(reportString);       
              messageProducer.send(replyMessage);
              return null;
            }
          }, true); //start connection
        } catch (Exception e) {
          e.printStackTrace();
          System.exit(1);
        } 
      }
    }).start();

    //test report is picked up correctly
    ConfigurationChangeEventReport report = processCommunicationManager.sendConfiguration(50L, Collections.EMPTY_LIST);
    assertNotNull(report);
    
  }

}
