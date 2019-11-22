package cern.c2mon.server.supervision.impl;

import cern.c2mon.shared.daq.process.ProcessConnectionRequest;
import cern.c2mon.shared.daq.process.ProcessDisconnectionRequest;
import org.junit.Before;

public class ProcessDisconnectionSupervisionTest extends AbstractSupervisionManagerProcessTest<ProcessDisconnectionRequest, Void> {

  public ProcessDisconnectionSupervisionTest() {
    super(processDisconnectionRequest -> null); // TODO Update this
  }

  @Before
  public void connectAndConfigure() throws Exception {
    String xmlResponse = supervisionManager.onProcessConnection(new ProcessConnectionRequest(GOOD_PROCESSNAME));
    xmlConverter.fromXml(xmlResponse);

    // TODO (Alex) PIK?
  }

//  @Test
//  public void onNull() {
//
//    processDisconnectionRequest = null;
//    onProcessDisconnection();
//
//    // Save the good PIK for the process running (for farther disconnection)
//    Long goodPIK = processConnectionResponse.getProcessPIK();
//
//    // Ignoring disconnection so new Connection will failed
//    onProcessConnection();
//
//    // PIK rejected second attempt
//    assertEquals(processConnectionResponse.getProcessPIK(), ProcessConnectionResponse.PIK_REJECTED);
//
//    // Set process PIK for correct disconnection
//    processConnectionResponse.setprocessPIK(goodPIK);
//  }
//
//  @Test
//  public void testOnProcessDisconnectionNoProcessNameAndID() {
//
//    processDisconnectionRequest = new ProcessDisconnectionRequest();
//    onProcessDisconnection();
//
//    // Save the good PIK for the process running (for farther disconnection)
//    Long goodPIK = processConnectionResponse.getProcessPIK();
//
//    // CacheElementNotFoundException cause it will not find anything in cache (neither process name nor ID)
//    // New Connection will failed
//    onProcessConnection();
//
//    // PIK rejected second attempt
//    assertEquals(processConnectionResponse.getProcessPIK(), ProcessConnectionResponse.PIK_REJECTED);
//
//    // Set process PIK for correct disconnection
//    processConnectionResponse.setprocessPIK(goodPIK);
//  }
//
//  @Test
//  public void testOnProcessDisconnectionBadPIK() {
//
//    processDisconnectionRequest = new ProcessDisconnectionRequest(GOOD_PROCESSNAME, NO_PIK,
//      processConnectionRequest.getProcessStartupTime().getTime());
//
//    onProcessDisconnection();
//
//    // Save the good PIK for the process running (for farther disconnection)
//    Long goodPIK = processConnectionResponse.getProcessPIK();
//
//    // Ignoring disconnection so new Connection will failed
//    onProcessConnection();
//
//    // PIK rejected second attempt
//    assertEquals(processConnectionResponse.getProcessPIK(), ProcessConnectionResponse.PIK_REJECTED);
//
//    // Set process PIK for correct disconnection
//    processConnectionResponse.setprocessPIK(goodPIK);
//  }
//
//  @Test
//  public void testOnProcessDisconnectionBadStartUpTime() {
//
//    processDisconnectionRequest = new ProcessDisconnectionRequest(GOOD_PROCESSNAME, processConnectionResponse.getProcessPIK(),
//      new Timestamp(System.currentTimeMillis()).getTime());
//
//    onProcessDisconnection();
//
//    // Nothing happens
//  }
//
//  @Test
//  public void testOnProcessDisconnectionStoppedProcess() {
//
//    processDisconnectionRequest = new ProcessDisconnectionRequest(GOOD_PROCESSNAME, processConnectionResponse.getProcessPIK(),
//      processConnectionRequest.getProcessStartupTime().getTime());
//
//    // First disconnection
//    onProcessDisconnection();
//
//    // Second disconnection
//    onProcessDisconnection();
//
//    // Nothing happens
//  }
//
//  @Test
//  public void testCompleteConnectionProcess() {
//
//    assertNotEquals(processConnectionResponse.getProcessName(), ProcessConnectionResponse.NO_PROCESS);
//    assertEquals(processConnectionRequest.getProcessName(), processConnectionResponse.getProcessName());
//    assertNotEquals(processConnectionResponse.getProcessPIK(), ProcessConnectionResponse.NO_PIK);
//    assertNotEquals(processConnectionResponse.getProcessPIK(), ProcessConnectionResponse.PIK_REJECTED);
//
//    // Configuration
//    processConfigurationRequest = new ProcessConfigurationRequest(processConnectionResponse.getProcessName());
//    processConfigurationRequest.setprocessPIK(processConnectionResponse.getProcessPIK());
//    onProcessConfiguration();
//
//    assertNotEquals(processConfigurationResponse.getProcessName(), ProcessConfigurationResponse.NO_PROCESS);
//    assertNotEquals(processConfigurationResponse.getConfigurationXML(), ProcessConfigurationResponse.NO_XML);
//    assertNotEquals(processConfigurationResponse.getConfigurationXML(), ProcessConfigurationResponse.CONF_REJECTED);
//
//    // Disconnection done automatic for every test
//  }
}
