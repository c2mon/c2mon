package cern.c2mon.server.supervision.impl;

import cern.c2mon.shared.daq.process.ProcessConnectionRequest;
import cern.c2mon.shared.daq.process.ProcessConnectionResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class ProcessConnectionSupervisionTest extends AbstractSupervisionManagerProcessTest<ProcessConnectionRequest, ProcessConnectionResponse> {

  public ProcessConnectionSupervisionTest() {
    super(request -> supervisionManager.onProcessConnection(request));
  }

  @Test
  public void onNull() throws Exception {
    doAndVerify(null,
      response -> assertEquals(response.getProcessPIK(), ProcessConnectionResponse.PIK_REJECTED));
  }

  @Test
  public void onBadName() throws Exception {
    doAndVerify(new ProcessConnectionRequest(BAD_PROCESSNAME),
      response -> assertEquals(response.getProcessPIK(), ProcessConnectionResponse.PIK_REJECTED));
  }

  @Test
  public void onRunningProcess() throws Exception {
    // Run the process the first time
    doAndVerify(new ProcessConnectionRequest(GOOD_PROCESSNAME),
      // PIK not rejected first attempt
      response -> assertNotEquals(response.getProcessPIK(), ProcessConnectionResponse.PIK_REJECTED)
      );

    // Second time the process is still running
    doAndVerify(new ProcessConnectionRequest(GOOD_PROCESSNAME),
      // PIK rejected second attempt
      response -> assertEquals(response.getProcessPIK(), ProcessConnectionResponse.PIK_REJECTED)
    );
  }
  
  @Test
  public void onValid() throws Exception {
    doAndVerify(new ProcessConnectionRequest(GOOD_PROCESSNAME),
      response -> {
        assertNotEquals(response.getProcessName(), ProcessConnectionResponse.NO_PROCESS);
        assertEquals(response.getProcessName(), response.getProcessName());
        assertNotEquals(response.getProcessPIK(), ProcessConnectionResponse.NO_PIK);
        assertNotEquals(response.getProcessPIK(), ProcessConnectionResponse.PIK_REJECTED);
      });
  }
}
