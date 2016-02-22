package cern.c2mon.server.configuration.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import javax.annotation.Resource;
import javax.sql.DataSource;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;

/**
 * This class is responsible for configuring the configuration DB datasource bean.
 *
 * A simple inspection is done on the JDBC URL to deduce whether to create an in-memory
 * in-process database, start a file-based in-process database or connect to an external
 * database. The former two cases will also cause the DDL scripts and any data scripts
 * to be run.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
public class ConfigDataSourceConfig {

  @Resource
  private Environment environment;

  @Bean
  @ConfigurationProperties(prefix = "datasource.config")
  public DataSource configurationDataSource() {
    String url = environment.getProperty("datasource.config.url");
    if (url == null || url.contains("hsql")) {
      return new EmbeddedDatabaseBuilder().setType(HSQL).setName("config").addScript("classpath:resources/sql/config-schema-generic.sql").build();
    } else {
      return DataSourceBuilder.create().build();
    }
  }

}
