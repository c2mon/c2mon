package cern.c2mon.server.configuration;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.cache.test.CachePopulationRule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.configuration.config.ConfigurationModule;
import cern.c2mon.server.configuration.config.ProcessCommunicationManagerMock;
import cern.c2mon.server.daq.config.DaqModule;
import cern.c2mon.server.daq.update.JmsContainerManagerImpl;
import cern.c2mon.server.rule.config.RuleModule;
import cern.c2mon.server.supervision.config.SupervisionModule;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheActionsModuleRef.class,
  CacheConfigModuleRef.class,
  CacheDbAccessModule.class,
  CacheLoadingModuleRef.class,
  SupervisionModule.class,
  ConfigurationModule.class,
  DaqModule.class,
  RuleModule.class,
  CachePopulationRule.class,
  ProcessCommunicationManagerMock.class,
  C2monIgniteConfiguration.class
})
public abstract class ConfigurationCacheTest {

//  @Rule
//  public Timeout forceTimeout = new Timeout(1, TimeUnit.SECONDS);

  @Inject
  protected JmsContainerManagerImpl jmsContainerManager;

  @After
  public void cleanUp() {
    // Make sure the JmsContainerManager is stopped, otherwise the
    // DefaultMessageListenerContainers inside will keep trying to connect to
    // a JMS broker (which will not be running)
    jmsContainerManager.stop();
  }
}
