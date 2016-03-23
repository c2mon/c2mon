package cern.c2mon.server.configuration.config;

import cern.c2mon.server.common.util.HsqlDatabaseBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * This class is responsible for configuring the configuration DB datasource bean.
 *
 * A simple inspection is done on the JDBC URL to deduce whether to create an in-memory
 * in-process database, start a file-based in-process database or connect to an external
 * database.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
public class ConfigDataSourceConfig {

  @Resource
  private Environment environment;

  @Bean
  @ConfigurationProperties(prefix = "c2mon.server.configuration.jdbc")
  public DataSource configurationDataSource() {
    String url = environment.getProperty("c2mon.server.configuration.jdbc.url");

    if (url.contains("hsql")) {
      return new HsqlDatabaseBuilder().setUrl(url).addScript(new ClassPathResource("resources/sql/config-schema-generic.sql")).build();
    } else {
      return DataSourceBuilder.create().build();
    }
  }
}
