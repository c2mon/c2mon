package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.server.configuration.ConfigurationCacheTest;
import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.server.daq.out.ProcessCommunicationManager;
import cern.c2mon.shared.common.Cacheable;
import org.junit.Before;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.Timestamp;

import static org.easymock.EasyMock.reset;

public abstract class ConfigurationCacheLoaderTest<T extends Cacheable> extends ConfigurationCacheTest {

  @Inject
  protected ConfigurationLoader configurationLoader;

  /**
   * Mocked daqcommunication-out module.
   */
  @Inject
  protected ProcessCommunicationManager mockManager;

  @Inject
  private ProcessService processService;

  @Before
  public void beforeTest() throws IOException {
    // make sure Process is "running" (o.w. nothing is sent to DAQ)
    processService.start(50L, "hostname", new Timestamp(System.currentTimeMillis()));

    reset(mockManager);
  }
}
