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
package cern.c2mon.server.client;

import cern.c2mon.server.client.junit.CachePopulationRule;
import cern.c2mon.server.client.junit.DatabasePopulationRule;
import cern.c2mon.server.client.request.ClientRequestReportHandler;
import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.server.configuration.impl.ConfigurationLoaderImpl;
import cern.c2mon.server.test.broker.TestBrokerService;
import cern.c2mon.server.test.device.ObjectComparison;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.device.DeviceClassNameResponse;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.client.device.TransferDevice;
import cern.c2mon.shared.client.request.ClientRequestImpl;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.easymock.EasyMock;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.*;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration test of client module with server core.
 *
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
    "classpath:config/server-client.xml",
    "classpath:config/server-cache.xml",
    "classpath:config/server-cachedbaccess.xml",
    "classpath:config/server-configuration.xml",
    "classpath:config/server-daqcommunication-in.xml",
    "classpath:config/server-daqcommunication-out.xml",
    "classpath:config/server-rule.xml",
    "classpath:config/server-supervision.xml",
    "classpath:config/server-alarm.xml",
    "classpath:config/server-command.xml",
    "classpath:test-config/server-test-properties.xml"})
@TestPropertySource("classpath:c2mon-server-default.properties")
public class ClientModuleIntegrationTest implements ApplicationContextAware {

  @Rule
  @Autowired
  public CachePopulationRule databasePopulationRule;

  @Value("${c2mon.server.client.jms.queue.admin}")
  private String adminQueue;

  @Value("${c2mon.server.client.jms.queue.request}")
  private String requestQueue;

  private ConfigurationLoader configurationLoader = EasyMock.createMock(ConfigurationLoaderImpl.class);
  
  private ApplicationContext applicationContext;

  private static TestBrokerService testBrokerService = new TestBrokerService();

  @BeforeClass
  public static void startJmsBroker() throws Exception {
    testBrokerService.createAndStartBroker();
  }

  @AfterClass
  public static void stopBroker() throws Exception {
    testBrokerService.stopBroker();
  }

  @Before
  public void setUp() {
    ((GenericApplicationContext) applicationContext).start();
  }

  /**
   * Fakes the client request and checks the server response is correct.
   */
  @Test
  @Ignore("what is the point of this test?")
  public void testConfigurationRequest() {
    EasyMock.reset(configurationLoader);
    final ConfigurationReport fakedReport = new ConfigurationReport(10, "config", "user_name");
    EasyMock.expect(configurationLoader.applyConfiguration(EasyMock.eq(10), EasyMock.isA(ClientRequestReportHandler.class))).andReturn(fakedReport);

    EasyMock.replay(configurationLoader);
    // send client request to admin queue
    JmsTemplate clientTemplate = new JmsTemplate(testBrokerService.getConnectionFactory());
    clientTemplate.execute(new SessionCallback<Object>() {

      @Override
      public Object doInJms(final Session session) throws JMSException {
        Destination replyDestination = session.createTemporaryQueue();
        TextMessage message = new ActiveMQTextMessage();
        message.setJMSReplyTo(replyDestination);
        ClientRequestImpl<ConfigurationReport> request = new ClientRequestImpl<ConfigurationReport>(ConfigurationReport.class);
        request.addTagId(10L);
        message.setText(request.toJson());
        MessageProducer producer = session.createProducer(new ActiveMQQueue(adminQueue));
        producer.send(message);
        MessageConsumer consumer = session.createConsumer(replyDestination);
        Message replyMessage = consumer.receive(1000);
        assertNotNull(replyMessage);
        assertTrue(replyMessage instanceof TextMessage);
        String replyText = ((TextMessage) replyMessage).getText();
        System.out.println("reply: " + replyText);
        Collection<ConfigurationReport> reportList = request.fromJsonResponse(replyText);
        assertNotNull(reportList);
        assertEquals(1, reportList.size());
        ConfigurationReport report = reportList.iterator().next();
        assertEquals(fakedReport.getId(), report.getId());
        assertEquals(fakedReport.getName(), report.getName());
        assertEquals(fakedReport.getUser(), report.getUser());
        assertEquals(fakedReport.getStatus(), report.getStatus());
        assertEquals(fakedReport.getStatusDescription(), report.getStatusDescription());
        return null;
      }
    }, true);

    EasyMock.verify(configurationLoader);
  }

  @Test
  public void testHandleDeviceClassNamesRequest() {
    // Reset the mock
    EasyMock.reset();

    // Setup is finished, need to activate the mock
    EasyMock.replay();

    JmsTemplate clientTemplate = new JmsTemplate(testBrokerService.getConnectionFactory());
    clientTemplate.execute(new SessionCallback<Object>() {

      @Override
      public Object doInJms(final Session session) throws JMSException {
        Destination replyDestination = session.createTemporaryQueue();
        TextMessage message = new ActiveMQTextMessage();
        message.setJMSReplyTo(replyDestination);

        ClientRequestImpl<DeviceClassNameResponse> request = new ClientRequestImpl<>(DeviceClassNameResponse.class);
        message.setText(request.toJson());

        MessageProducer producer = session.createProducer(new ActiveMQQueue(requestQueue));
        producer.send(message);
        MessageConsumer consumer = session.createConsumer(replyDestination);
        Message replyMessage = consumer.receive(1000);
        assertNotNull(replyMessage);
        assertTrue(replyMessage instanceof TextMessage);
        String replyText = ((TextMessage) replyMessage).getText();

        List<DeviceClassNameResponse> deviceClassNames = (List<DeviceClassNameResponse>) request.fromJsonResponse(replyText);
        assertNotNull(deviceClassNames);
        assertEquals(2, deviceClassNames.size());
        assertTrue(deviceClassNames.get(1).getDeviceClassName().equals("TEST_DEVICE_CLASS_1"));
        assertTrue(deviceClassNames.get(0).getDeviceClassName().equals("TEST_DEVICE_CLASS_2"));

        return null;
      }
    }, true);

    // Verify that everything happened as expected
    EasyMock.verify();
  }

  @Test
  public void testHandleDeviceRequest() throws JMSException {
    // Reset the mock
    EasyMock.reset();

    // Setup is finished, need to activate the mock
    EasyMock.replay();

    JmsTemplate clientTemplate = new JmsTemplate(testBrokerService.getConnectionFactory());
    clientTemplate.execute(new SessionCallback<Object>() {

      @Override
      public Object doInJms(final Session session) throws JMSException {
        Destination replyDestination = session.createTemporaryQueue();
        TextMessage message = new ActiveMQTextMessage();
        message.setJMSReplyTo(replyDestination);

        ClientRequestImpl<TransferDevice> request = new ClientRequestImpl<>(TransferDevice.class);
        request.setRequestParameter("TEST_DEVICE_CLASS_1");
        message.setText(request.toJson());

        MessageProducer producer = session.createProducer(new ActiveMQQueue(requestQueue));
        producer.send(message);
        MessageConsumer consumer = session.createConsumer(replyDestination);
        Message replyMessage = consumer.receive(5000);
        assertNotNull(replyMessage);
        assertTrue(replyMessage instanceof TextMessage);
        String replyText = ((TextMessage) replyMessage).getText();

        List<TransferDevice> devices = (List<TransferDevice>) request.fromJsonResponse(replyText);
        assertNotNull(devices);
        assertEquals(2, devices.size());

        TransferDevice device1 = devices.get(0);
        assertNotNull(device1);
        assertEquals(device1.getId(), new Long(300));
        assertEquals(device1.getName(), "TEST_DEVICE_1");
        assertEquals(device1.getDeviceClassId(), new Long(400));
        assertEquals(device1.getDeviceClassName(), "TEST_DEVICE_CLASS_1");

        List<DeviceProperty> deviceProperties = device1.getDeviceProperties();
        assertNotNull(deviceProperties);
        assertEquals(deviceProperties.size(), 4);

        try {
          ObjectComparison.assertDevicePropertyListContains(deviceProperties, new DeviceProperty(1L, "cpuLoadInPercent", "987654", "tagId", "String"));
          ObjectComparison.assertDevicePropertyListContains(deviceProperties, new DeviceProperty(2L, "responsiblePerson", "Mr. Administrator", "constantValue",
              "String"));
          ObjectComparison.assertDevicePropertyListContains(deviceProperties, new DeviceProperty(3L, "someCalculations", "(#123 + #234) / 2", "clientRule",
              "Float"));
          ObjectComparison.assertDevicePropertyListContains(deviceProperties, new DeviceProperty(4L, "numCores", "4", "constantValue", "Integer"));
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }

        List<DeviceCommand> deviceCommands = device1.getDeviceCommands();
        assertNotNull(deviceCommands);
        assertEquals(deviceCommands.size(), 1);
        assertEquals(deviceCommands.get(0).getName(), "TEST_COMMAND_1");

        return null;
      }
    }, true);

    // Verify that everything happened as expected
    EasyMock.verify();
  }

  @Override
  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}
