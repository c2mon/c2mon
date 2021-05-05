package cern.c2mon.server.supervision;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.impl.configuration.IgniteModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.cache.test.CachePopulationRule;
import cern.c2mon.server.cache.test.SupervisionCacheResetRule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.supervision.config.SupervisionModule;
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
  IgniteModule.class,
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
