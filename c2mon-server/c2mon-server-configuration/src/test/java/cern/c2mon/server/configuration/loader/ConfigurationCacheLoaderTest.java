package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.server.configuration.ConfigurationCacheTest;
import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.server.configuration.impl.ConfigurationLoaderImpl;
import cern.c2mon.server.configuration.junit.ConfigRuleChain;
import cern.c2mon.server.configuration.util.CacheObjectFactory;
import cern.c2mon.server.configuration.util.TestConfigurationProvider;
import cern.c2mon.server.daq.out.ProcessCommunicationManager;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;

import javax.inject.Inject;
import java.sql.Timestamp;

import static org.easymock.EasyMock.*;

@Slf4j
public abstract class ConfigurationCacheLoaderTest<T extends Cacheable> extends ConfigurationCacheTest {

  @Rule
  @Inject
  public ConfigRuleChain configRuleChain;

  @Inject
  protected ConfigurationLoader configurationLoader;

  @Inject
  protected CacheObjectFactory cacheObjectFactory;

  @Inject
  protected ProcessService processService;

  /**
   * Mocked daqcommunication-out module.
   */
  @Inject
  protected ProcessCommunicationManager mockManager;

  @Before
  public void beforeTest() {
    ((ConfigurationLoaderImpl) configurationLoader).setDaqConfigEnabled(true);
    reset(mockManager);
    try {
      expect(mockManager.sendConfiguration(anyLong(), anyObject())).andReturn(new ConfigurationChangeEventReport()).anyTimes();
    } catch (Exception e) {
      log.info("Failed to set up mock interceptor for the DAQ config report");
    }
    replay(mockManager);
  }

  protected void setUp() {
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    Configuration createEquipment = TestConfigurationProvider.createEquipment();
    configurationLoader.applyConfiguration(createEquipment);
    Configuration createSubEquipment = TestConfigurationProvider.createSubEquipment();
    configurationLoader.applyConfiguration(createSubEquipment);
    Configuration createDataTag = TestConfigurationProvider.createEquipmentDataTag(15L);
    configurationLoader.applyConfiguration(createDataTag);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));
  }
}
