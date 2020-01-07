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
package cern.c2mon.server.daq.out;


import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.daq.config.DaqModule;
import cern.c2mon.server.daq.junit.DaqCachePopulationRule;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.server.test.broker.EmbeddedBrokerRule;
import cern.c2mon.server.test.config.TestConfig;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import cern.c2mon.shared.daq.serialization.MessageConverter;
import org.apache.activemq.command.ActiveMQQueue;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.*;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;

/**
 * Integration test of ProcessCommunicationManager with rest of core.
 *
 * @author Mark Brightwell
 *
 */
@Ignore("This test is flaky")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheConfigModuleRef.class,
    CacheDbAccessModule.class,
    CacheLoadingModuleRef.class,
    SupervisionModule.class,
    DaqModule.class,
    TestConfig.class,
})
public class ProcessCommunicationManagerTest {

  @Rule
  @Autowired
  public DaqCachePopulationRule daqCachePopulationRule;

  @Rule
  @Autowired
  public EmbeddedBrokerRule brokerRule;

  @Autowired
  @Qualifier("daqOutActiveMQConnectionFactory")
  private ConnectionFactory connectionFactory;

  /**
   * To test.
   */
  @Autowired
  private ProcessCommunicationManager processCommunicationManager;

  @Autowired
  private C2monCache<Process> processCache;

  /**
   * Tests request is sent and response is processed. Connects to in-memory
   * broker.
   */
  @Test
  public void testConfigurationRequest() throws Exception {
    //fake DAQ responding to request
    final JmsTemplate daqTemplate = new JmsTemplate(connectionFactory);
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          daqTemplate.execute(new SessionCallback<Object>() {
            String reportString = MessageConverter.responseToJson(new ConfigurationChangeEventReport());
            @Override
            public Object doInJms(Session session) throws JMSException {
              Process process = processCache.get(50L);
              String jmsDaqQueue = "c2mon.process" + ".command." + process.getCurrentHost() + "." + process.getName() + "." + process.getProcessPIK();
              MessageConsumer consumer = session.createConsumer(new ActiveMQQueue(jmsDaqQueue));
              Message incomingMessage = consumer.receive(1000);
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
