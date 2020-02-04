package cern.c2mon.server.configuration.loader;

import cern.c2mon.server.configuration.ConfigurationCacheTest;
import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.server.configuration.junit.ConfigRuleChain;
import cern.c2mon.server.configuration.util.CacheObjectFactory;
import cern.c2mon.server.daq.out.ProcessCommunicationManager;
import cern.c2mon.shared.common.Cacheable;
import org.junit.Before;
import org.junit.Rule;

import javax.inject.Inject;

import static org.easymock.EasyMock.reset;

public abstract class ConfigurationCacheLoaderTest<T extends Cacheable> extends ConfigurationCacheTest {

  @Rule
  @Inject
  public ConfigRuleChain configRuleChain;

  @Inject
  protected ConfigurationLoader configurationLoader;

  @Inject
  protected CacheObjectFactory cacheObjectFactory;

  /**
   * Mocked daqcommunication-out module.
   */
  @Inject
  protected ProcessCommunicationManager mockManager;

  @Before
  public void beforeTest() {
    reset(mockManager);
  }
}
