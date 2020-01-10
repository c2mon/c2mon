package cern.c2mon.server.supervision.process;

import cern.c2mon.shared.daq.process.ProcessConnectionRequest;
import cern.c2mon.shared.daq.process.ProcessConnectionResponse;
import cern.c2mon.shared.daq.process.ProcessDisconnectionRequest;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.function.Consumer;

import static cern.c2mon.shared.daq.process.ProcessConnectionResponse.PIK_REJECTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ProcessDisconnectionSupervisionTest extends AbstractSupervisionManagerProcessTest<ProcessConnectionRequest, ProcessConnectionResponse> {
  private long pik;
  private Timestamp startupTime;

  @Before
  public void connectAndConfigure() throws Exception {
    startupTime = new Timestamp(System.currentTimeMillis());
    String xmlResponse = supervisionManager.onProcessConnection(new ProcessConnectionRequest(GOOD_PROCESSNAME, startupTime));
    pik = ((ProcessConnectionResponse) xmlConverter.fromXml(xmlResponse)).getProcessPIK();
  }

  @Test
  public void onNull() throws Exception {
    supervisionManager.onProcessDisconnection(null);

    doAndVerify(new ProcessConnectionRequest(GOOD_PROCESSNAME),
      processConnectionResponse ->
        assertEquals(processConnectionResponse.getProcessPIK(), PIK_REJECTED));
  }

  @Test
  public void testOnProcessDisconnectionNoProcessNameAndID() throws Exception {
    supervisionManager.onProcessDisconnection(new ProcessDisconnectionRequest());

    doAndVerify(new ProcessConnectionRequest(GOOD_PROCESSNAME),
      processConnectionResponse ->
        assertEquals(processConnectionResponse.getProcessPIK(), PIK_REJECTED));
  }

  private void doAndVerify(ProcessConnectionRequest request, Consumer<ProcessConnectionResponse> tests) throws Exception {
    doAndVerify(request, supervisionManager::onProcessConnection, tests);
  }

  @Test
  public void testOnProcessDisconnectionBadPIK() throws Exception {
    supervisionManager.onProcessDisconnection(new ProcessDisconnectionRequest(GOOD_PROCESSNAME, ProcessDisconnectionRequest.NO_PIK, startupTime.getTime()));

    doAndVerify(new ProcessConnectionRequest(GOOD_PROCESSNAME),
      processConnectionResponse ->
        assertEquals(processConnectionResponse.getProcessPIK(), PIK_REJECTED));
  }

  @Test
  public void testOnProcessDisconnectionBadStartUpTime() throws Exception {
    supervisionManager.onProcessDisconnection(new ProcessDisconnectionRequest(GOOD_PROCESSNAME, ProcessDisconnectionRequest.NO_PIK, startupTime.getTime() + 1));

    doAndVerify(new ProcessConnectionRequest(GOOD_PROCESSNAME),
      processConnectionResponse ->
        assertEquals(processConnectionResponse.getProcessPIK(), PIK_REJECTED));
  }

  @Test
  public void testOnValidProcessDisconnection() throws Exception {
    supervisionManager.onProcessDisconnection(new ProcessDisconnectionRequest(GOOD_PROCESSNAME, pik, startupTime.getTime() + 1));

    doAndVerify(new ProcessConnectionRequest(GOOD_PROCESSNAME),
      response -> {
        assertEquals(GOOD_PROCESSNAME, response.getProcessName());
        assertNotEquals(ProcessConnectionResponse.NO_PROCESS, response.getProcessName());
        assertNotEquals(ProcessConnectionResponse.NO_PIK, response.getProcessPIK());
        assertNotEquals(ProcessConnectionResponse.PIK_REJECTED, response.getProcessPIK());
      });
  }
}
