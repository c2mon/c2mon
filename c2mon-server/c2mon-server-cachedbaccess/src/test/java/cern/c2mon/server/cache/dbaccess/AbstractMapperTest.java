package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.test.DatabasePopulationRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
    CommonModule.class,
    CacheDbAccessModule.class,
    DatabasePopulationRule.class
})
public abstract class AbstractMapperTest {

  @Rule
  @Inject
  public DatabasePopulationRule databasePopulationRule;
}
