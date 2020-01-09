package cern.c2mon.server.history;


import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.command.config.CommandModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.daq.config.DaqModule;
import cern.c2mon.server.history.config.HistoryModule;
import cern.c2mon.server.supervision.config.SupervisionModule;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheActionsModuleRef.class,
  CacheConfigModuleRef.class,
  CacheDbAccessModule.class,
  CacheLoadingModuleRef.class,
  C2monIgniteConfiguration.class,
  CommandModule.class,
  SupervisionModule.class,
  DaqModule.class,
  HistoryModule.class
})
public abstract class MapperTest {
}
