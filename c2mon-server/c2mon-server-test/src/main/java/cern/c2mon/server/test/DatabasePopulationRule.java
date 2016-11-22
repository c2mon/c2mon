package cern.c2mon.server.test;

import org.junit.rules.ExternalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

/**
 * @author Justin Lewis Salmon
 */
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
