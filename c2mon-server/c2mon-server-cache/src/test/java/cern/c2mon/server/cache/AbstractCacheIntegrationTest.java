package cern.c2mon.server.cache;

import cern.c2mon.server.cache.config.CacheModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.junit.CachePopulationRule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModule;
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
@ContextConfiguration(classes = {
    CacheModule.class,
    CacheDbAccessModule.class,
    CacheLoadingModule.class,
    CachePopulationRule.class
})
@TestPropertySource(properties = {
    // TODO: remove these
    "c2mon.server.client.jms.topic.tag.trunk=c2mon.client.tag",
    "c2mon.server.client.jms.topic.controltag=c2mon.client.controltag",
    "c2mon.server.daqcommunication.jms.queue.trunk=c2mon.process"
})
public abstract class AbstractCacheIntegrationTest {

  @Rule
  @Autowired
  public CachePopulationRule cachePopulationRule;
}
