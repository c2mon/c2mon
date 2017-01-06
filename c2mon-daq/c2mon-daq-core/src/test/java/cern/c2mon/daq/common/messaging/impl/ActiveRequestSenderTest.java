/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.common.messaging.impl;

import static org.easymock.EasyMock.eq;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import cern.c2mon.daq.common.conf.core.ProcessConfigurationHolder;
import cern.c2mon.daq.config.DaqCoreModule;
import cern.c2mon.daq.config.DaqProperties;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.shared.daq.process.ProcessConfigurationRequest;
import cern.c2mon.shared.daq.process.ProcessConfigurationResponse;
import cern.c2mon.shared.daq.process.ProcessConnectionRequest;
import cern.c2mon.shared.daq.process.ProcessConnectionResponse;
import cern.c2mon.shared.daq.process.ProcessDisconnectionRequest;
import cern.c2mon.shared.daq.process.ProcessMessageConverter;
import cern.c2mon.shared.daq.process.XMLConverter;

/**
 * Integration test of ActiveRequestSenderTest with broker.
 * Testing PIK process request
 *
 * @author Nacho Vilches
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    DaqCoreModule.class
})
@TestPropertySource(
    value = "classpath:c2mon-daq-default.properties",
    properties = {
        "c2mon.daq.name=P_TESTHANDLER03",
        "jms.broker.url=vm://localhost:61616?broker.persistent=false&broker.useShutdownHook=false&broker.useJmx=false"
    }
)
public class ActiveRequestSenderTest {

  /**
   * The system's logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ActiveRequestSenderTest.class);

  /**
   * Constant of the PIK request time out
   */
  private static final long TEST_RESULTS_TIMEOUT = 12000;

  /**
   * Constant of the Process name to be used
   */
  private static final String PROCESS_NAME = "P_TESTHANDLER03";

  /**
   * Constant of the emulated PIK
   */
  private static final Long PROCESS_PIK = 12345L;

  /**
   * Constant of the PIK REJECTED
   */
  private static final Long PIK_REJECTED = 0L;

  /**
   * Test Type enum
   */
  public static enum TestType {
    CONNECT_SUCCESS("testSuccessfullSendprocessConnectionRequest"),
    CONNECT_REJECT("testRejectSendprocessConnectionRequest"),
    CONNECT_BAD_XML("testBadXMLSendprocessConnectionRequest"),
    CONNECT_TIME_OUT("testTimeOutSendprocessConnectionRequest"),
    CONNECT_BAD_PIK_OBJECT("testBadPIKObjectSendprocessConnectionRequest"),
    DISCONNECT("testSendProcessDisconnectionRequest"),
    CONFIG("testSendProcessConfigurationRequest");

    /**
     * Test name
     */
    private String name;

    /**
     * Set test name
     *
     * @param name Test name
     */
    TestType(final String name) {
      this.name = name;
    }

    /**
     * Get test name
     *
     * @return Test name
     */
    public final String getName() {
      return this.name;
    }
  }

  /**
   * Test type to prepare reply
   */
  private static TestType testType;

  /**
   * The class to test
   */
  private ActiveRequestSender activeRequestSender = null;

  /**
   * Instantiated in XML.
   */
  @Autowired
  @Qualifier("processRequestJmsTemplate")
  private JmsTemplate jmsTemplate;

  /**
   * Listener (for starting/stopping manually the listener)
   */
  @Autowired
  private DefaultMessageListenerContainer testMessageListenerContainer;

  private DaqProperties properties = new DaqProperties();

  /**
   * Mock
   * Configuration controller as access point to the configuration.
   */
  private ConfigurationController configurationControllerMock = null;

  /**
   * Mock
   * The process configuration object.
   */
  private ProcessConfiguration processConfigurationMock;

  /**
   * Before each tests
   */
  @Before
  public final void setUp() {
    LOGGER.debug("Setting up");

    // Start listener
    this.testMessageListenerContainer.start();

    // Use of org.easymock.classextension.EasyMoc to mock classes

    properties.setName(PROCESS_NAME);

    this.configurationControllerMock = EasyMock.createMockBuilder(ConfigurationController.class).
        addMockedMethod("getProcessConfiguration").
        createMock();

    this.processConfigurationMock = EasyMock.createMockBuilder(ProcessConfiguration.class).
        addMockedMethod("getProcessID").
        addMockedMethod("getProcessName").
        addMockedMethod("getprocessPIK").
        createMock();

    // Class to test ActiveRequestSender
    this.activeRequestSender = new ActiveRequestSender(this.properties, this.jmsTemplate);
  }

  /**
   * After each tests
   */
  @After
  public final void afterTest() {
    // Stop listener
    this.testMessageListenerContainer.stop();
  }

  /**
   * Test Successful Send ProcessPIK Request
   */
  @Test
  @Ignore
  public final void testSuccessfulSendprocessConnectionRequest() {
    ActiveRequestSenderTest.testType = TestType.CONNECT_SUCCESS;
    LOGGER.debug("Starting " + ActiveRequestSenderTest.testType.getName());

    // Call the sending process
    sendProcessConnectionRequest();
  }

  /**
   * Test Reject Send ProcessPIK Request
   */
  @Test
  @Ignore
  public final void testRejectSendprocessConnectionRequest() {
    ActiveRequestSenderTest.testType = TestType.CONNECT_REJECT;
    LOGGER.debug("Starting " + ActiveRequestSenderTest.testType.getName());

    // Call the sending process
    sendProcessConnectionRequest();
  }

  /**
   * Test Bad XML Send ProcessPIK Request
   */
  @Test(expected = Exception.class)
  @Ignore
  public final void testBadXMLSendprocessConnectionRequest() {
    ActiveRequestSenderTest.testType = TestType.CONNECT_BAD_XML;
    LOGGER.debug("Starting " + ActiveRequestSenderTest.testType.getName());

    // Call the sending process
    sendProcessConnectionRequest();
  }

  /**
   * Test Time Out Send ProcessPIK Request
   */
  @Test
  @Ignore
  public final void testTimeOutSendprocessConnectionRequest() {
    ActiveRequestSenderTest.testType = TestType.CONNECT_TIME_OUT;
    LOGGER.debug("Starting " + ActiveRequestSenderTest.testType.getName());

    // Call the sending process
    sendProcessConnectionRequest();
  }

  /**
   * This method do all the mocking work before and after calling the sendprocessConnectionRequest()
   * function. It is common for all tests cause the differences are in the reply messages
   *
   */
  private void sendProcessConnectionRequest() {

    // Start mock replay
    EasyMock.replay(this.configurationControllerMock);

    // Here, the real test starts
    ProcessConnectionResponse processConnectionResponse = this.activeRequestSender.sendProcessConnectionRequest(PROCESS_NAME);

    // onMessage call will take over the reply. No need top wait since the call will never come back before without a reply

    // Check return result of sendprocessConnectionRequest() and compare it against your emulated server answer.
    compareConnection(processConnectionResponse);

    // Verify configurationController Mock to check that sendprocessConnectionRequest() called what we expected
    EasyMock.verify(this.configurationControllerMock);
  }

  /**
   * Test Send ProcessDisconnection Request
   */
  @Test
  @Ignore
  public final void testSendProcessDisconnectionRequest() {
    ActiveRequestSenderTest.testType = TestType.DISCONNECT;
    LOGGER.debug("Starting " + ActiveRequestSenderTest.testType.getName());

    // Call the sending process disconnection
    sendProcessDisconnectionRequest();
  }

  /**
   * This method do all the mocking work before and after calling the sendProcessDisonnectionRequest()
   * function. It is common for all tests cause the differences are in the reply messages
   *
   */
  private void sendProcessDisconnectionRequest() {
    // Expectations.

    EasyMock.expect(this.configurationControllerMock.getProcessConfiguration()).andReturn(this.processConfigurationMock).times(1);
    EasyMock.expect(this.processConfigurationMock.getProcessID()).andReturn(-1L).times(1, 2);
    EasyMock.expect(this.processConfigurationMock.getProcessName()).andReturn(PROCESS_NAME).times(1, 3);
    EasyMock.expect(this.processConfigurationMock.getprocessPIK()).andReturn(PROCESS_PIK).times(1, 2);

    // Start mock replay
    EasyMock.replay(this.configurationControllerMock, this.processConfigurationMock);

    // Here, the real test starts
    this.activeRequestSender.sendProcessDisconnectionRequest(this.configurationControllerMock.getProcessConfiguration(), -1L);

    // Verify configurationController Mock to check that sendprocessConnectionRequest() called what we expected
    EasyMock.verify(this.configurationControllerMock, this.processConfigurationMock);
  }

  /**
   * Test Send ProcessDisconnection Request
   */
  @Test
  @Ignore
  public final void testSendProcessConfigurationRequest() {
    ActiveRequestSenderTest.testType = TestType.CONFIG;
    LOGGER.debug("Starting " + ActiveRequestSenderTest.testType.getName());

    ProcessConfiguration processConfiguration = new ProcessConfiguration();
    processConfiguration.setProcessName(PROCESS_NAME);
    processConfiguration.setprocessPIK(PROCESS_PIK);
    ProcessConfigurationHolder.setInstance(processConfiguration);

    // Here, the real test starts
    ProcessConfigurationResponse processConfigurationResponse = this.activeRequestSender.sendProcessConfigurationRequest(PROCESS_NAME);

    // Check return result of sendProcessConfigurationRequest() and compare it against your emulated server answer.
    compareConfiguration(processConfigurationResponse);
  }


  /**
   * Listener used for emulating the server
   *
   * @author Nacho Vilches
   *
   */
  static class TestMessageListener implements SessionAwareMessageListener<Message> {


    /**
     * ProcessMessageConverter helper class (fromMessage/ToMessage)
     */
    private ProcessMessageConverter processMessageConverter = new ProcessMessageConverter();

    /**
     * XML Converter helper class
     */
    private XMLConverter xmlConverter = new XMLConverter();

    @Override
    public synchronized void onMessage(final Message message, final Session session) throws JMSException {
      try {
        LOGGER.debug("onMessage() - Message coming " + message);
        String text = ((TextMessage) message).getText();
        Object processRequest = this.processMessageConverter.fromJSON(text);
        LOGGER.debug("onMessage() - Message converted " + processRequest.toString());

        // ProcessDisconnectionRequest
        if (processRequest instanceof ProcessDisconnectionRequest) {
          LOGGER.debug("onMessage() - Process disconnection completed for DAQ " + ((ProcessDisconnectionRequest) processRequest).getProcessName());

        }
        // ProcessConnectionRequest
        else if (processRequest instanceof ProcessConnectionRequest) {
          ProcessConnectionRequest processConnectionRequest = (ProcessConnectionRequest) processRequest;
          LOGGER.info("onMessage - DAQ Connection request received from DAQ " + processConnectionRequest.getProcessName());

          // Replace the server original call
          //String processConnectionResponse = supervisionManager.onProcessConnection(processConnectionRequest);
          String stringProcessConnectionResponse = null;

          // Create the ProcessConnectionResponse
          ProcessConnectionResponse processConnectionResponse = new ProcessConnectionResponse();
          processConnectionResponse.setProcessName(processConnectionRequest.getProcessName());

          // Different tests types
          if (ActiveRequestSenderTest.testType != TestType.CONNECT_TIME_OUT) {
            if (ActiveRequestSenderTest.testType == TestType.CONNECT_SUCCESS) {
              // With the emulated good server reply
              processConnectionResponse.setProcessPIK(PROCESS_PIK);
              stringProcessConnectionResponse = this.xmlConverter.toXml(processConnectionResponse);
              LOGGER.debug("Good reply sent to PIK Request");
            } else if (ActiveRequestSenderTest.testType == TestType.CONNECT_REJECT) {
              // With the emulated Rejected server reply
              processConnectionResponse.setProcessPIK(PIK_REJECTED);
              stringProcessConnectionResponse = this.xmlConverter.toXml(processConnectionResponse);
              LOGGER.debug("Rejected reply sent to PIK Request");
            } else if (ActiveRequestSenderTest.testType == TestType.CONNECT_BAD_XML) {
              // Modify the XML
              stringProcessConnectionResponse = this.xmlConverter.toXml("Nacho testing XML");
              LOGGER.debug("Bad XML reply sent to PIK Request");
            }
          } else {
            // Time Out (normally it is 12000 for the timeout)
            try {
              Thread.sleep(TEST_RESULTS_TIMEOUT);
            } catch (InterruptedException e) {
              LOGGER.debug("Time Out Thread error: " + e);
            }
            LOGGER.debug("No reply sent to PIK Request");
          }

          LOGGER.debug("onMessage - Sending Connection response to DAQ " + processConnectionRequest.getProcessName());

          MessageProducer messageProducer = session.createProducer(message.getJMSReplyTo());
          try {
            TextMessage replyMessage = session.createTextMessage();
            replyMessage.setText(stringProcessConnectionResponse);
            messageProducer.send(replyMessage);
          } finally {
            messageProducer.close();
          }
        }
        // ProcessConfigurationRequest
        else if (processRequest instanceof ProcessConfigurationRequest) {
          ProcessConfigurationRequest processConfigurationRequest = (ProcessConfigurationRequest) processRequest;
          LOGGER.info("onMessage - DAQ configuration request received from DAQ " + processConfigurationRequest.getProcessName());

          ProcessConfigurationResponse processConfigurationResponse = new ProcessConfigurationResponse();
          processConfigurationResponse.setProcessName(processConfigurationRequest.getProcessName());

          // Replace the server original call
//          String processConfiguration = supervisionManager.onProcessConfiguration(processConfigurationRequest);

          // We get the configuration XML file (empty by default)
          String path = new ClassPathResource("P_JECTEST01-testhandler.xml").getFile().getAbsolutePath();
          processConfigurationResponse.setConfigurationXML(readFile(path));


          String processConfiguration = this.xmlConverter.toXml(processConfigurationResponse);

          LOGGER.debug("onMessage - Sending Configuration Response to DAQ " + processConfigurationResponse.getProcessName());

          MessageProducer messageProducer = session.createProducer(message.getJMSReplyTo());
          try {
            TextMessage replyMessage = session.createTextMessage();
            replyMessage.setText(processConfiguration);
            messageProducer.send(replyMessage);
          } finally {
            messageProducer.close();
          }
        } else {
          LOGGER.error("onMessage - Incoming ProcessRequest object not recognized! - ignoring the request");
        }
      } catch (MessageConversionException e) {
        LOGGER.error("onMessage - Exception caught while converting incoming DAQ request - unable to process request", e);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public String readFile(String filename) {
      String content = null;
      File file = new File(filename); //for ex foo.txt
      try {
        FileReader reader = new FileReader(file);
        char[] chars = new char[(int) file.length()];
        reader.read(chars);
        content = new String(chars);
        reader.close();
      } catch (IOException e) {
        LOGGER.error("readFile - File " + filename.toString());
        e.printStackTrace();
      }
      return content;
    }

  }

  /**
   * This method checks all the Connection replies behave as expected.
   *
   * @param processConnectionResponse the processConnectionResponse object returned from the sendprocessConnectionRequest()
   */
  private synchronized static void compareConnection(final ProcessConnectionResponse processConnectionResponse) {
    if (ActiveRequestSenderTest.testType == TestType.CONNECT_SUCCESS) {
      assertNotNull(processConnectionResponse);
      assertEquals(PROCESS_NAME, processConnectionResponse.getProcessName());
      assertEquals(PROCESS_PIK, processConnectionResponse.getProcessPIK());
    } else if (ActiveRequestSenderTest.testType == TestType.CONNECT_REJECT) {
      assertNotNull(processConnectionResponse);
      assertEquals(PROCESS_NAME, processConnectionResponse.getProcessName());
      assertEquals(PIK_REJECTED, processConnectionResponse.getProcessPIK());
    } else if (ActiveRequestSenderTest.testType == TestType.CONNECT_TIME_OUT) {
      assertEquals(null, processConnectionResponse);
    } else {
      assertNotNull(null, "ERROR - No test for PIK was found.");
    }
  }

  /**
   * This method checks all the Configuration replies behave as expected.
   *
   * @param processConfigurationResponse
   */
  private synchronized static void compareConfiguration(final ProcessConfigurationResponse processConfigurationResponse) {
    if (ActiveRequestSenderTest.testType == TestType.CONFIG) {
      assertNotNull(processConfigurationResponse);
      assertEquals(PROCESS_NAME, processConfigurationResponse.getProcessName());
    }
  }
}
