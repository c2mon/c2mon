/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2013 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common.messaging.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.daq.common.conf.core.CommonConfiguration;
import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.conf.core.ProcessConfiguration;
import cern.c2mon.daq.common.conf.core.RunOptions;
import cern.c2mon.daq.common.messaging.impl.ActiveRequestSender;
import cern.c2mon.daq.common.messaging.impl.backward.ProcessRequestResponseConverterBC;
import cern.c2mon.daq.tools.CommandParamsHandler;
import cern.tim.shared.daq.process.ProcessConfigurationRequest;
import cern.tim.shared.daq.process.ProcessConfigurationResponse;
import cern.tim.shared.daq.process.ProcessConnectionRequest;
import cern.tim.shared.daq.process.ProcessConnectionResponse;
import cern.tim.shared.daq.process.ProcessDisconnectionRequest;
import cern.tim.shared.daq.process.ProcessMessageConverter;
import cern.tim.shared.daq.process.ProcessRequest;
import cern.tim.shared.daq.process.XMLConverter;
import cern.tim.shared.daq.process.backward.ProcessRequestConverterBC;

/**
 * Integration test of ActiveRequestSenderTest with broker.
 * Testing PIK process request
 * 
 * @author Nacho Vilches
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:resources/daq-test-properties.xml" , "classpath:resources/daq-activemq.xml", 
"classpath:resources/daq-test-activerequestsender.xml"})
public class ActiveRequestSenderTest {

  /**
   * The system's logger
   */
  private static final Logger LOGGER = Logger.getLogger(ActiveRequestSenderTest.class);

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

  /**
   * Mock
   * Configuration controller as access point to the configuration.
   */
  private ConfigurationController configurationControllerMock = null;

  /**
   * Mock
   * Reference to the command line parameter object.
   */
  private CommandParamsHandler commandParamsHandlerMock = null;
  
  /**
   * Mock
   * The run options of the DAQ process.
   */
  private RunOptions runOptionsMock = null;
  
  /**
   * Mock
   * The process configuration object.
   */
  private ProcessConfiguration processConfigurationMock;
  
  /**
   * Mock
   * The CommonConfiguration object to be used in the Configuration test call
   */
  private CommonConfiguration commonConfigurationMock;
  
  /**
   * TODO: Backward compatibility. remove after updating server
   */
  
  /**
   * Reference to converter called explicitly when sending the connection
   * request.
   */
  @Autowired
  private ProcessRequestConverterBC processRequestConverterBC;
  
  /**
   * Reference to the converter for processing the reply (converts Message ->
   * DOM document).
   */
  @Autowired
 private ProcessRequestResponseConverterBC processRequestResponseConverterBC;


  /**
   * Before each tests
   */
  @Before
  public final void setUp() {  
    LOGGER.debug("Setting up");

    // Start listener
    this.testMessageListenerContainer.start();

    // Use of org.easymock.classextension.EasyMoc to mock classes

    // Mock for CommandParamsHandler to use getParamValue(String) 
    // String[] args = {"-processName", "P_TESTHANDLER03"};
    this.commandParamsHandlerMock = EasyMock.createMockBuilder(CommandParamsHandler.class).
        //        withConstructor(String[].class).
        //        withArgs((Object)args).
        addMockedMethod("getParamValue", String.class).
        createMock();
    
    this.runOptionsMock = EasyMock.createMockBuilder(RunOptions.class).
        addMockedMethod("getStartUp").
        createMock();

    // Mock for configurationController to use getCommandParamsHandler()
    // The run options of the DAQ process
    this.configurationControllerMock = EasyMock.createMockBuilder(ConfigurationController.class).
        withConstructor(RunOptions.class, CommonConfiguration.class).
        withArgs(null, null).
        addMockedMethod("getCommandParamsHandler").
        addMockedMethod("getRunOptions").
        addMockedMethod("getProcessConfiguration").
        createMock();  
    
    this.processConfigurationMock = EasyMock.createMockBuilder(ProcessConfiguration.class).
        addMockedMethod("getProcessID").
        addMockedMethod("getProcessName").
        addMockedMethod("getprocessPIK").
        createMock(); 
    
    this.commonConfigurationMock  = EasyMock.createMockBuilder(CommonConfiguration.class).
        addMockedMethod("getRequestTimeout").
        createMock(); 

    // Class to test ActiveRequestSender
    this.activeRequestSender = new ActiveRequestSender(this.commonConfigurationMock, this.jmsTemplate, processRequestConverterBC,  
        processRequestResponseConverterBC, this.configurationControllerMock);
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
    // Expectations.
    EasyMock.expect(this.configurationControllerMock.getCommandParamsHandler()).andReturn(this.commandParamsHandlerMock).times(1);
    EasyMock.expect(this.commandParamsHandlerMock.getParamValue("-processName")).andReturn(PROCESS_NAME).times(1);
    
    EasyMock.expect(this.configurationControllerMock.getRunOptions()).andReturn(this.runOptionsMock).times(1);
    //EasyMock.expect(this.runOptionsMock.setStartUp(System.currentTimeMillis())).times(1);

    // Start mock replay
    EasyMock.replay(this.commandParamsHandlerMock, this.configurationControllerMock, this.runOptionsMock);  

    // Here, the real test starts
    ProcessConnectionResponse processConnectionResponse = this.activeRequestSender.sendProcessConnectionRequest();
    
//    try {
//      Thread.sleep(2000);
//    }
//    catch (InterruptedException e) {
//      LOGGER.error("testSendprocessConnectionRequest() - Sleep error. "+ e);
//    }

    // onMessage call will take over the reply. No need top wait since the call will never come back before without a reply

    // Check return result of sendprocessConnectionRequest() and compare it against your emulated server answer.    
    compareConnection(processConnectionResponse);

    // Verify configurationController Mock to check that sendprocessConnectionRequest() called what we expected
    EasyMock.verify(this.commandParamsHandlerMock, this.configurationControllerMock, this.runOptionsMock);
  }
  
  /**
   * Test Send ProcessDisconnection Request
   */
  @Test
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
    EasyMock.expect(this.configurationControllerMock.getRunOptions()).andReturn(this.runOptionsMock).times(1);
    EasyMock.expect(this.runOptionsMock.getStartUp()).andReturn(System.currentTimeMillis()).times(1);
    
    EasyMock.expect(this.configurationControllerMock.getProcessConfiguration()).andReturn(this.processConfigurationMock).times(1);
    EasyMock.expect(this.processConfigurationMock.getProcessID()).andReturn(-1L).times(1, 2);
    EasyMock.expect(this.processConfigurationMock.getProcessName()).andReturn(PROCESS_NAME).times(1, 3);
    EasyMock.expect(this.processConfigurationMock.getprocessPIK()).andReturn(PROCESS_PIK).times(1, 2);

    // Start mock replay
    EasyMock.replay(this.configurationControllerMock, this.runOptionsMock, this.processConfigurationMock);  

    // Here, the real test starts
    this.activeRequestSender.sendProcessDisconnectionRequest();

    // Verify configurationController Mock to check that sendprocessConnectionRequest() called what we expected
    EasyMock.verify(this.configurationControllerMock, this.runOptionsMock, this.processConfigurationMock);
  }
  
  /**
   * Test Send ProcessDisconnection Request
   */
  @Test
  public final void testSendProcessConfigurationRequest() {
    ActiveRequestSenderTest.testType = TestType.CONFIG;
    LOGGER.debug("Starting " + ActiveRequestSenderTest.testType.getName());

    // Call the sending process configuration
    sendProcessConfigurationRequest();
  }
  
  /**
   * This method do all the mocking work before and after calling the sendProcessConfigurationRequest()
   * function. It is common for all tests cause the differences are in the reply messages
   * 
   */
  private void sendProcessConfigurationRequest() {
    // Expectations.
    EasyMock.expect(this.configurationControllerMock.getCommandParamsHandler()).andReturn(this.commandParamsHandlerMock).times(1);
    EasyMock.expect(this.commandParamsHandlerMock.getParamValue("-processName")).andReturn(PROCESS_NAME).times(1);
    EasyMock.expect(this.commonConfigurationMock.getRequestTimeout()).andReturn(TEST_RESULTS_TIMEOUT).times(2);
    EasyMock.expect(this.configurationControllerMock.getProcessConfiguration()).andReturn(this.processConfigurationMock).times(1);
    EasyMock.expect(this.processConfigurationMock.getprocessPIK()).andReturn(PROCESS_PIK).times(1);

    // Start mock replay
    EasyMock.replay(this.commandParamsHandlerMock, this.configurationControllerMock, this.commonConfigurationMock, this.processConfigurationMock); 

    // Here, the real test starts
    ProcessConfigurationResponse processConfigurationResponse = this.activeRequestSender.sendProcessConfigurationRequest();
    
    // Check return result of sendProcessConfigurationRequest() and compare it against your emulated server answer.    
    compareConfiguration(processConfigurationResponse);
    
    // Verify configurationController Mock to check that sendprocessConnectionRequest() called what we expected
    EasyMock.verify(this.commandParamsHandlerMock, this.configurationControllerMock, this.commonConfigurationMock);
  }
  
  

  /**
   * Listener declared
   *  in daq-test-activerequestsender.xml and used for
   * emulating the server
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
        ProcessRequest processRequest = (ProcessRequest) this.processMessageConverter.fromMessage(message);
        LOGGER.debug("onMessage() - Message converted " + processRequest.toString());
        
        // ProcessDisconnectionRequest
        if (processRequest instanceof ProcessDisconnectionRequest) {          
          LOGGER.debug("onMessage() - Process disconnection completed for DAQ " + ((ProcessDisconnectionRequest) processRequest).getProcessName());
      
        } 
        // ProcessConnectionRequest
        else if (processRequest instanceof ProcessConnectionRequest) {  
          ProcessConnectionRequest processConnectionRequest = (ProcessConnectionRequest)processRequest;
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
              processConnectionResponse.setprocessPIK(PROCESS_PIK);
              stringProcessConnectionResponse = this.xmlConverter.toXml(processConnectionResponse);
              LOGGER.debug("Good reply sent to PIK Request");
            } 
            else if (ActiveRequestSenderTest.testType == TestType.CONNECT_REJECT) {
              // With the emulated Rejected server reply
              processConnectionResponse.setprocessPIK(PIK_REJECTED);
              stringProcessConnectionResponse = this.xmlConverter.toXml(processConnectionResponse);
              LOGGER.debug("Rejected reply sent to PIK Request");
            }
            else if (ActiveRequestSenderTest.testType == TestType.CONNECT_BAD_XML) {
              // Modify the XML
              stringProcessConnectionResponse = this.xmlConverter.toXml("Nacho testing XML");
              LOGGER.debug("Bad XML reply sent to PIK Request");
            }         
          } 
          else {
            // Time Out (normally it is 12000 for the timeout)
            try {
              Thread.sleep(TEST_RESULTS_TIMEOUT);
            }
            catch (InterruptedException e) {
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
          URL url = Test.class.getClassLoader().getResource("resources/P_JECTEST01-testhandler.xml");
          processConfigurationResponse.setConfigurationXML(readFile(url.getPath())); 
         
          
          
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
      }   
    }
    
    public String readFile(String filename)
    {
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
    }
    else if (ActiveRequestSenderTest.testType == TestType.CONNECT_REJECT) {
      assertNotNull(processConnectionResponse);
      assertEquals(PROCESS_NAME, processConnectionResponse.getProcessName());
      assertEquals(PIK_REJECTED, processConnectionResponse.getProcessPIK());
    }
    else if (ActiveRequestSenderTest.testType == TestType.CONNECT_TIME_OUT) {
      assertEquals(null, processConnectionResponse);
    }
    else {
      assertNotNull(null, "ERROR - No test for PIK was found.");
    }
  }  
  
  /**
   * This method checks all the Configuration replies behave as expected.
   * 
   * @param processConnectionResponse the processConnectionResponse object returned from the sendprocessConnectionRequest()
   */
  private synchronized static void compareConfiguration(final ProcessConfigurationResponse processConfigurationResponse) {
    if (ActiveRequestSenderTest.testType == TestType.CONFIG) {
      assertNotNull(processConfigurationResponse);
      assertEquals(PROCESS_NAME, processConfigurationResponse.getProcessName());
    }
  }  
}
