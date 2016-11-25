package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.test.DatabasePopulationRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheDbAccessModule.class,
    DatabasePopulationRule.class
})
public abstract class AbstractMapperTest {

  @Rule
  @Autowired
  public DatabasePopulationRule databasePopulationRule;
}
