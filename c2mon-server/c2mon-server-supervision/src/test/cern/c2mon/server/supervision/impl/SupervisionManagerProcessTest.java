package cern.c2mon.server.supervision.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.supervision.SupervisionManager;
import cern.c2mon.shared.daq.process.ProcessConfigurationRequest;
import cern.c2mon.shared.daq.process.ProcessConfigurationResponse;
import cern.c2mon.shared.daq.process.ProcessConnectionRequest;
import cern.c2mon.shared.daq.process.ProcessConnectionResponse;
import cern.c2mon.shared.daq.process.ProcessDisconnectionRequest;
import cern.c2mon.shared.daq.process.XMLConverter;

/**
 * Integration test of supervision module for all Process Messaging (PIK, Connection, Disconnection)
 * 
 * @author Nacho Vilches
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/supervision/config/server-supervision-integration.xml" })
public class SupervisionManagerProcessTest {
  
  /**
   * The system's logger
   */
  private static final Logger LOGGER = Logger.getLogger(SupervisionManagerProcessTest.class);

  
  @Autowired
  private SupervisionManager supervisionManager;
  
  /**
   * Constant of GOOD_PROCESSNAME
   */
  private static final String GOOD_PROCESSNAME = "P_TESTHANDLER03";
  
  /**
   * Constant of BAD_PROCESSNAME
   */
  private static final String BAD_PROCESSNAME = "P_TESTNACHO";
  
  /**
   * Constant of the NO_PIK as default value
   */
  public static final Long NO_PIK = -1L;
  
  /**
   * Process PIK Request
   */
  private ProcessConnectionRequest processConnectionRequest;
  
  /**
   * Process PIK Response
   */
  private ProcessConnectionResponse processConnectionResponse;
  
  /**
   * Process Configuration Request
   */
  private ProcessConfigurationRequest processConfigurationRequest;
  
  /**
   * Process Configuration Response
   */
  private ProcessConfigurationResponse processConfigurationResponse;
  
  /**
   * Process Disconnection Request
   */
  private ProcessDisconnectionRequest processDisconnectionRequest;
  
  /**
   * XML Converter helper class
   */
  private XMLConverter xmlConverter = new XMLConverter();
  

  @Before
  public void setUp() {  
    this.processConnectionRequest = null;
    this.processConnectionResponse = null;
    this.processConfigurationRequest = null;
    this.processConfigurationResponse = null;
    this.processDisconnectionRequest = null;
//    this.processDisconnectionBC = null;
  }
  
  @After
  public void disconnection() {  
    // Disconnection
    if((this.processConnectionResponse != null) && (this.processConnectionRequest != null) /*&& (this.processDisconnectionBC == null)*/) {
      this.processDisconnectionRequest = new ProcessDisconnectionRequest(GOOD_PROCESSNAME, this.processConnectionResponse.getProcessPIK(), 
          this.processConnectionRequest.getProcessStartupTime().getTime());
      
      onProcessDisconnection();
    }
  }
  
  @Test
  public void testOnProcessConnectionNull() {
    LOGGER.info("Starting test testOnProcessConnectionNull");
    this.processConnectionRequest = null;
    
    onProcessConnection();
    
    assertEquals(this.processConnectionResponse.getProcessPIK(), ProcessConnectionResponse.PIK_REJECTED);
  }
  
  @Test
  public void testOnProcessConnectionBadProcessName() {
    LOGGER.info("Starting test testOnProcessConnectionBadProcessName");
    this.processConnectionRequest = new ProcessConnectionRequest(BAD_PROCESSNAME);
    
    onProcessConnection();
    
    assertEquals(this.processConnectionResponse.getProcessPIK(), ProcessConnectionResponse.PIK_REJECTED);
  }
  
  @Test
  public void testOnProcessConnectionRunningProcess() {
    LOGGER.info("Starting test testOnProcessConnectionRunningProcess");
    this.processConnectionRequest = new ProcessConnectionRequest(GOOD_PROCESSNAME);
    
    // Run the process the first time
    onProcessConnection();
    
    // PIK not rejected first attempt
    assertFalse(this.processConnectionResponse.getProcessPIK().equals(ProcessConnectionResponse.PIK_REJECTED));

    
    // Second time the process is still running
    onProcessConnection();
    
    // PIK rejected second attempt
    assertEquals(this.processConnectionResponse.getProcessPIK(), ProcessConnectionResponse.PIK_REJECTED);
  }
  
  @Test
  public void testOnProcessConnection() {
    LOGGER.info("Starting test testOnProcessConnection");
    this.processConnectionRequest = new ProcessConnectionRequest(GOOD_PROCESSNAME);
    
    onProcessConnection();
    
    assertFalse(this.processConnectionResponse.getProcessName().equals(ProcessConnectionResponse.NO_PROCESS));
    assertEquals(this.processConnectionRequest.getProcessName(), this.processConnectionResponse.getProcessName());
    assertFalse(this.processConnectionResponse.getProcessPIK().equals(ProcessConnectionResponse.NO_PIK));
    assertFalse(this.processConnectionResponse.getProcessPIK().equals(ProcessConnectionResponse.PIK_REJECTED));
  }
  
  @Test
  public void testOnProcessConfigurationNull() {
    LOGGER.info("Starting test testOnProcessConfigurationNull");
    
    this.processConfigurationRequest = null;
    
    onProcessConfiguration();
    
    assertEquals(processConfigurationResponse.getConfigurationXML(), ProcessConfigurationResponse.CONF_REJECTED);
  }
  
  @Test
  public void testOnProcessConfigurationBadProcessName() {
    LOGGER.info("Starting test testOnProcessConfigurationBadProcessName");
    this.processConfigurationRequest = new ProcessConfigurationRequest(BAD_PROCESSNAME);
    onProcessConfiguration();
    
    assertEquals(this.processConfigurationResponse.getConfigurationXML(), ProcessConfigurationResponse.CONF_REJECTED);
  }
  
  @Test
  public void testOnProcessConfigurationGoodPIK() {
    LOGGER.info("Starting test testOnProcessConfigurationGoodPIK");
    this.processConnectionRequest = new ProcessConnectionRequest(GOOD_PROCESSNAME);
    
    onProcessConnection();
    
    assertEquals(this.processConnectionRequest.getProcessName(), this.processConnectionResponse.getProcessName());
    assertFalse(this.processConnectionResponse.getProcessPIK().equals(ProcessConnectionResponse.PIK_REJECTED));
    
    // Configuration
    this.processConfigurationRequest = new ProcessConfigurationRequest(this.processConnectionResponse.getProcessName());
    onProcessConfiguration();
    
    assertFalse(this.processConfigurationResponse.getConfigurationXML().equals(ProcessConfigurationResponse.CONF_REJECTED));
  }
  
  @Test
  public void testOnProcessDisconnectionNull() {
    LOGGER.info("Starting test testOnProcessDisconnectionNull");
    
    this.processConnectionRequest = new ProcessConnectionRequest(GOOD_PROCESSNAME);
    onProcessConnection();
    
    this.processDisconnectionRequest = null;
    onProcessDisconnection();
    
    // Save the good PIK for the process running (for farther disconnection)
    Long goodPIK = this.processConnectionResponse.getProcessPIK();
    
    // Ignoring disconnection so new Connection will failed
    onProcessConnection();
    
    // PIK rejected second attempt
    assertEquals(this.processConnectionResponse.getProcessPIK(), ProcessConnectionResponse.PIK_REJECTED);
    
    // Set process PIK for correct disconnection
    this.processConnectionResponse.setprocessPIK(goodPIK);
  }
  
  @Test
  public void testOnProcessDisconnectionNoProcessNameAndID() {
    LOGGER.info("Starting test testOnProcessDisconnectionNoProcessNameAndID");
    
    this.processConnectionRequest = new ProcessConnectionRequest(GOOD_PROCESSNAME);
    onProcessConnection();
    
    this.processDisconnectionRequest = new ProcessDisconnectionRequest();
    onProcessDisconnection();
    
    // Save the good PIK for the process running (for farther disconnection)
    Long goodPIK = this.processConnectionResponse.getProcessPIK();
    
    // CacheElementNotFoundException cause it will not find anything in cache (neither process name nor ID)
    // New Connection will failed
    onProcessConnection();
    
    // PIK rejected second attempt
    assertEquals(this.processConnectionResponse.getProcessPIK(), ProcessConnectionResponse.PIK_REJECTED);
    
    // Set process PIK for correct disconnection
    this.processConnectionResponse.setprocessPIK(goodPIK);
  }
  
  @Test
  public void testOnProcessDisconnectionBadPIK() {
    LOGGER.info("Starting test testOnProcessDisconnectionBadPIK");
    
    this.processConnectionRequest = new ProcessConnectionRequest(GOOD_PROCESSNAME);
    onProcessConnection();
    
    this.processDisconnectionRequest = new ProcessDisconnectionRequest(GOOD_PROCESSNAME, NO_PIK, 
        this.processConnectionRequest.getProcessStartupTime().getTime());
    
    onProcessDisconnection();
    
    // Save the good PIK for the process running (for farther disconnection)
    Long goodPIK = this.processConnectionResponse.getProcessPIK();
    
    // Ignoring disconnection so new Connection will failed
    onProcessConnection();
    
    // PIK rejected second attempt
    assertEquals(this.processConnectionResponse.getProcessPIK(), ProcessConnectionResponse.PIK_REJECTED);
    
    // Set process PIK for correct disconnection
    this.processConnectionResponse.setprocessPIK(goodPIK);
  }
  
  @Test
  public void testOnProcessDisconnectionBadStartUpTime() {
    LOGGER.info("Starting test testOnProcessDisconnectionBadStartUpTime");
    
    this.processConnectionRequest = new ProcessConnectionRequest(GOOD_PROCESSNAME);
    
    onProcessConnection();
    
    this.processDisconnectionRequest = new ProcessDisconnectionRequest(GOOD_PROCESSNAME, this.processConnectionResponse.getProcessPIK(), 
        new Timestamp(System.currentTimeMillis()).getTime());
    
    onProcessDisconnection();
    
    // Nothing happens
  }
  
  @Test
  public void testOnProcessDisconnectionStoppedProcess() {
    LOGGER.info("Starting test testOnProcessDisconnectionStoppedProcess");
    
    this.processConnectionRequest = new ProcessConnectionRequest(GOOD_PROCESSNAME);
    onProcessConnection();

    this.processDisconnectionRequest = new ProcessDisconnectionRequest(GOOD_PROCESSNAME, this.processConnectionResponse.getProcessPIK(), 
        this.processConnectionRequest.getProcessStartupTime().getTime());
    
    // First disconnection
    onProcessDisconnection();
    
    // Second disconnection
    onProcessDisconnection();
    
    // Nothing happens
  }
  
  @Test
  public void testCompleteConnectionProcess() {
    LOGGER.info("Starting test testCompleteConnectionProcess");
    
    // Connection
    this.processConnectionRequest = new ProcessConnectionRequest(GOOD_PROCESSNAME);
    onProcessConnection();
    
    assertFalse(this.processConnectionResponse.getProcessName().equals(ProcessConnectionResponse.NO_PROCESS));
    assertEquals(this.processConnectionRequest.getProcessName(), this.processConnectionResponse.getProcessName());
    assertFalse(this.processConnectionResponse.getProcessPIK().equals(ProcessConnectionResponse.NO_PIK));
    assertFalse(this.processConnectionResponse.getProcessPIK().equals(ProcessConnectionResponse.PIK_REJECTED));
    
    // Configuration
    this.processConfigurationRequest = new ProcessConfigurationRequest(processConnectionResponse.getProcessName());
    this.processConfigurationRequest.setprocessPIK(this.processConnectionResponse.getProcessPIK());
    onProcessConfiguration();
    
    assertFalse(this.processConfigurationResponse.getProcessName().equals(ProcessConfigurationResponse.NO_PROCESS));
    assertFalse(this.processConfigurationResponse.getConfigurationXML().equals(ProcessConfigurationResponse.NO_XML));
    assertFalse(this.processConfigurationResponse.getConfigurationXML().equals(ProcessConfigurationResponse.CONF_REJECTED));
    
    // Disconnection done automatic for every test
  }
  
  /**
   * Process Connection call
   */
  public void onProcessConnection() {    
    LOGGER.info("onProcessConnection - Connection");
    
    LOGGER.info(this.processConnectionRequest);
    
    String xmlprocessConnectionResponse = this.supervisionManager.onProcessConnection(this.processConnectionRequest); 
    assertNotNull(xmlprocessConnectionResponse);
    
    LOGGER.info(xmlprocessConnectionResponse);
    
    try {
      this.processConnectionResponse = (ProcessConnectionResponse) this.xmlConverter.fromXml(xmlprocessConnectionResponse);
    }
    catch (Exception e) {
      LOGGER.error(e);
    }
    assertNotNull(this.processConnectionResponse);
    LOGGER.info(this.processConnectionResponse);
  }
  
  /**
   * Process Configuration call
   */
  public void onProcessConfiguration() {  
    LOGGER.info("onProcessConfiguration - Configuration");
    
    LOGGER.info(this.processConfigurationRequest);
    
    String xmlProcessConfigurationResponse = this.supervisionManager.onProcessConfiguration(this.processConfigurationRequest); 
    assertNotNull(xmlProcessConfigurationResponse);
    LOGGER.info(xmlProcessConfigurationResponse);
    
    try {
      this.processConfigurationResponse = (ProcessConfigurationResponse) this.xmlConverter.fromXml(xmlProcessConfigurationResponse);
    }
    catch (Exception e) {
      LOGGER.error(e);
    }
    assertNotNull(this.processConfigurationResponse);
    LOGGER.info(this.processConfigurationResponse);
  }
  
  /**
   * Process Disconnection call
   */
  public void onProcessDisconnection() {   
    LOGGER.info("Disconnection");
    
    LOGGER.info(processDisconnectionRequest);
    
    this.supervisionManager.onProcessDisconnection(this.processDisconnectionRequest); 
  } 
}
