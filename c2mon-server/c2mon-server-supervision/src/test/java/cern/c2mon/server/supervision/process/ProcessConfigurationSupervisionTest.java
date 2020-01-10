package cern.c2mon.server.supervision.process;

import cern.c2mon.shared.daq.process.ProcessConfigurationRequest;
import cern.c2mon.shared.daq.process.ProcessConfigurationResponse;
import cern.c2mon.shared.daq.process.ProcessConnectionRequest;
import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ProcessConfigurationSupervisionTest extends AbstractSupervisionManagerProcessTest<ProcessConfigurationRequest, ProcessConfigurationResponse> {

  @Test
  public void onNull() throws Exception {
    doAndVerify(null,
      response -> assertEquals(response.getConfigurationXML(), ProcessConfigurationResponse.CONF_REJECTED));
  }

  @Test
  public void onBadName() throws Exception {
    doAndVerify(new ProcessConfigurationRequest(BAD_PROCESSNAME),
      response -> assertEquals(response.getConfigurationXML(), ProcessConfigurationResponse.CONF_REJECTED));
  }

  @Test
  public void onGoodPIK() throws Exception {
    supervisionManager.onProcessConnection(new ProcessConnectionRequest(GOOD_PROCESSNAME));

    doAndVerify(
      new ProcessConfigurationRequest(GOOD_PROCESSNAME),
      response -> assertNotEquals(response.getConfigurationXML(), ProcessConfigurationResponse.CONF_REJECTED)
    );
  }

  private void doAndVerify(ProcessConfigurationRequest request, Consumer<ProcessConfigurationResponse> tests) throws Exception {
    doAndVerify(request, supervisionManager::onProcessConfiguration, tests);
  }
}
