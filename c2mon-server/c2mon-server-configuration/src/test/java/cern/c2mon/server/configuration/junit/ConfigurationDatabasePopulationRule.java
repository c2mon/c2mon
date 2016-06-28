package cern.c2mon.server.configuration.junit;

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
@Service
public class ConfigurationDatabasePopulationRule extends ExternalResource {

  @Autowired
  private DataSource configurationDataSource;

  @Override
  protected void before() {
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
        new ClassPathResource("sql/config-test-data.sql")
    );
    DatabasePopulatorUtils.execute(populator, configurationDataSource);
  }
}
