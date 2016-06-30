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

import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.test.broker.TestBrokerService;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import cern.c2mon.shared.daq.serialization.MessageConverter;
import org.apache.activemq.command.ActiveMQQueue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;

/**
 * Integration test of ProcessCommunicationManager with rest of core.
 *
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
    "classpath:config/server-cache.xml",
    "classpath:config/server-cachedbaccess.xml",
    "classpath:config/server-cacheloading.xml",
    "classpath:config/server-daqcommunication-out.xml",
    "classpath:test-config/server-test-properties.xml"
})
@TestPropertySource("classpath:c2mon-server-default.properties")
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
  @Ignore
  public void testConfigurationRequest() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException {
    //fake DAQ responding to request
    final JmsTemplate daqTemplate = new JmsTemplate(testBrokerService.getConnectionFactory());
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          daqTemplate.execute(new SessionCallback<Object>() {
            String reportString = MessageConverter.responseToJson(new ConfigurationChangeEventReport());
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
