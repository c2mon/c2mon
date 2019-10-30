package cern.c2mon.cache.config.junit;

import org.junit.rules.ExternalResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Using this rule in a JUnit test will ensure that all caches are preloaded
 * cleanly with test data at the start of each test.
 *
 * @author Justin Lewis Salmon
 */
public class CachePopulationRule extends ExternalResource {

  @Inject
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
