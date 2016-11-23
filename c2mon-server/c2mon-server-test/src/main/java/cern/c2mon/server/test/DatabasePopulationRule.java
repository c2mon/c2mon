package cern.c2mon.server.test;

import org.junit.rules.ExternalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

/**
 * Use this class if you need a cleanly populated backup database at the
 * start of your tests.
 *
 * Example:
 *
 * @RunWith(SpringJUnit4ClassRunner.class)
 * @ContextConfiguration(classes = {
 *    CacheDbAccessModule.class,
 *    CacheLoadingModule.class,
 *    DatabasePopulationRule.class
 * })
 * public class ControlTagLoaderDAOTest {
 *
 *    @Rule
 *    @Autowired
 *    public DatabasePopulationRule databasePopulationRule;
 *
 *    @Test
 *    ...
 * }
 *
 * @author Justin Lewis Salmon
 */
@Configuration
public class DatabasePopulationRule extends ExternalResource {

  @Autowired
  private DataSource cacheDataSource;

  @Override
  protected void before() {
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
        new ClassPathResource("sql/cache-data-remove.sql"),
        new ClassPathResource("sql/cache-data-insert.sql")
    );
    DatabasePopulatorUtils.execute(populator, cacheDataSource);
  }
}
