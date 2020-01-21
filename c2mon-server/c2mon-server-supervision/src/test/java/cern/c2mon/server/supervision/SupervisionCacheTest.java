package cern.c2mon.server.supervision;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.server.test.CachePopulationRule;
import cern.c2mon.server.test.SupervisionCacheResetRule;
import org.junit.Rule;
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
  C2monIgniteConfiguration.class,
  CachePopulationRule.class,
  SupervisionCacheResetRule.class,
  SupervisionModule.class
})
public abstract class SupervisionCacheTest {

  @Rule
  @Inject
  public SupervisionCacheResetRule supervisionCacheResetRule;

  @Inject
  protected SupervisionManager supervisionManager;

  @Inject
  protected SupervisionNotifier supervisionNotifier;
}
