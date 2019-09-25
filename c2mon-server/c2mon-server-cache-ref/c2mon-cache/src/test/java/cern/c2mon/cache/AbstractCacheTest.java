package cern.c2mon.cache;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.cache.junit.CachePopulationRule;
import cern.c2mon.server.cache.CacheModuleRef;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loader.config.CacheLoaderModuleRef;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.shared.common.Cacheable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
  CacheModuleRef.class,
  CacheDbAccessModule.class,
  CacheLoaderModuleRef.class,
  CachePopulationRule.class,
  C2monIgniteConfiguration.class
})
public abstract class AbstractCacheTest<V extends Cacheable> {

  @Rule
  @Autowired
  public CachePopulationRule cachePopulationRule;

  protected C2monCache<V> cache;
  protected Long existingKey;

  @Before
  public void reload() {
    if (cache == null)
      cache = getCache();
    if (existingKey == null)
      existingKey = getExistingKey();
    cache.clear();
    assertEquals(0, cache.getAll(cache.getKeys()).size());
    cache.init();
  }

  protected abstract Long getExistingKey();

  protected abstract C2monCache<V> getCache();
}

