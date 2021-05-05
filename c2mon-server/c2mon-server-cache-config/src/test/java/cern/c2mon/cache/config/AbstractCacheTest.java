package cern.c2mon.cache.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.impl.configuration.IgniteModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
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
  IgniteModule.class
})
public abstract class AbstractCacheTest<V extends Cacheable> {

  protected abstract V getSample();

  @Rule
  @Inject
  public DatabasePopulationRule databasePopulationRule;

  protected C2monCache<V> cache;

  @Before
  public void reload() {
    if (cache == null)
      cache = getCache();
    cache.clear();
    assertEquals(0, cache.getAll(cache.getKeys()).size());
    cache.init();
  }

  protected abstract C2monCache<V> getCache();
}

