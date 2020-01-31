package cern.c2mon.cache;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.cache.test.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.common.AbstractCacheableImpl;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.test.DatabasePopulationRule;
import cern.c2mon.shared.common.Cacheable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * A common base for running cache tests
 *
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheConfigModuleRef.class,
  CacheDbAccessModule.class,
  CacheLoadingModuleRef.class,
  DatabasePopulationRule.class,
  C2monIgniteConfiguration.class,
  CacheActionsModuleRef.class
})
public abstract class AbstractCacheTest<CACHEABLE extends Cacheable, CACHEABLE_IMPL extends AbstractCacheableImpl> {
  // TODO (Alex) Refactor this into a single generic type when the rest of the CacheObjects are merged

  @Rule
  @Inject
  public DatabasePopulationRule cachePopulationRule;

  protected C2monCache<CACHEABLE> cache;

  protected AbstractCacheObjectFactory<CACHEABLE_IMPL> factory;

  @Before
  public void reload() {
    factory = initFactory();
    if (cache == null)
      cache = initCache();
    cache.clear();
    assertEquals(0, cache.getAll(cache.getKeys()).size());
    cache.init();
  }

  protected final CACHEABLE getSample() {
    return (CACHEABLE) factory.sampleBase();
  }

  protected abstract C2monCache<CACHEABLE> initCache();

  protected abstract AbstractCacheObjectFactory<CACHEABLE_IMPL> initFactory();
}

