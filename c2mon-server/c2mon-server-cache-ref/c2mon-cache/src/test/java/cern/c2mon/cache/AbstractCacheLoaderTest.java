package cern.c2mon.cache;

import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.cache.junit.CachePopulationRule;
import cern.c2mon.server.cache.CacheModuleRef;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loader.config.CacheLoaderModuleRef;
import cern.c2mon.server.common.config.CommonModule;

/**
 * @author Szymon Halastra
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
public abstract class AbstractCacheLoaderTest {

  @Rule
  @Autowired
  public CachePopulationRule cachePopulationRule;
}
