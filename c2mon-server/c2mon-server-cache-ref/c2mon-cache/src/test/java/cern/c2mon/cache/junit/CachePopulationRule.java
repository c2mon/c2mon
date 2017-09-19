package cern.c2mon.cache.junit;

import javax.sql.DataSource;

import org.junit.rules.ExternalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alarm.Alarm;

/**
 * Using this rule in a JUnit test will ensure that all caches are preloaded
 * cleanly with test data at the start of each test.
 *
 * @author Justin Lewis Salmon
 */
public class CachePopulationRule extends ExternalResource {

  @Autowired
  private DataSource cacheDataSource;

  @Autowired
  private C2monCache<Long, Alarm> alarmCacheRef;


  @Override
  protected void before() {
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
            new ClassPathResource("sql/cache-data-remove.sql"),
            new ClassPathResource("sql/cache-data-insert.sql")
    );
    DatabasePopulatorUtils.execute(populator, cacheDataSource);
  }
}
