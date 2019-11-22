package cern.c2mon.server.supervision.impl;

import cern.c2mon.shared.daq.process.ProcessConfigurationRequest;
import cern.c2mon.shared.daq.process.ProcessConfigurationResponse;
import cern.c2mon.shared.daq.process.ProcessConnectionRequest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ProcessConfigurationSupervisionTest extends AbstractSupervisionManagerProcessTest<ProcessConfigurationRequest, ProcessConfigurationResponse> {

  public ProcessConfigurationSupervisionTest() {
    super(processConfigurationRequest -> supervisionManager.onProcessConfiguration(processConfigurationRequest));
  }

  @Before
  public void sendGoodRequest() throws Exception {
    supervisionManager.onProcessConnection(new ProcessConnectionRequest(GOOD_PROCESSNAME));
  }

  @Test
  public void onNull() throws Exception {
    doAndVerify(null,
      response -> assertEquals(response.getConfigurationXML(), ProcessConfigurationResponse.CONF_REJECTED));
  }

  @Test
  public void onBadName() throws Exception {
//    processConnectionRequest = new ProcessConnectionRequest(GOOD_PROCESSNAME);
//
//    onProcessConnection();
//
//    // Configuration
//    processConfigurationRequest = new ProcessConfigurationRequest(processConnectionResponse.getProcessName());
//    onProcessConfiguration();
//
//    ;
    doAndVerify(new ProcessConfigurationRequest(GOOD_PROCESSNAME),
      response -> {
        assertNotEquals(response.getConfigurationXML(), ProcessConfigurationResponse.CONF_REJECTED);
      });
  }

  @Test
  public void onGoodPIK() throws Exception {
//    this.processConnectionRequest = new ProcessConnectionRequest(GOOD_PROCESSNAME);
//
//    onProcessConnection();
//
//    assertEquals(this.processConnectionRequest.getProcessName(), this.processConnectionResponse.getProcessName());
//    assertFalse(this.processConnectionResponse.getProcessPIK().equals(ProcessConnectionResponse.PIK_REJECTED));
//
//    // Configuration
//    this.processConfigurationRequest = new ProcessConfigurationRequest(this.processConnectionResponse.getProcessName());
//    onProcessConfiguration();
//
//    ;
    doAndVerify(new ProcessConfigurationRequest(GOOD_PROCESSNAME),
      response -> assertNotEquals(response.getConfigurationXML(), ProcessConfigurationResponse.CONF_REJECTED));
  }
}
