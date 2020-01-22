package cern.c2mon.server.configuration.junit;

import org.junit.rules.ExternalResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

/**
 * @author Justin Lewis Salmon
 */
@Named
public class ConfigurationDatabasePopulationRule extends ExternalResource {

  @Inject
  private DataSource configurationDataSource;

  @Override
  protected void before() {
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
        new ClassPathResource("sql/config-test-data.sql")
    );
    DatabasePopulatorUtils.execute(populator, configurationDataSource);
  }
}
