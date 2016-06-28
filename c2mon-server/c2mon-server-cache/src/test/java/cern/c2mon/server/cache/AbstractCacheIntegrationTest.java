package cern.c2mon.server.cache;

import cern.c2mon.server.cache.junit.CachePopulationRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
    "classpath:config/server-cache.xml",
    "classpath:config/server-cachedbaccess.xml",
    "classpath:config/server-cacheloading.xml",
    "classpath:test-config/server-test-properties.xml"
})
@TestPropertySource("classpath:c2mon-server-default.properties")
public abstract class AbstractCacheIntegrationTest {

  @Rule
  @Autowired
  public CachePopulationRule cachePopulationRule;
}
