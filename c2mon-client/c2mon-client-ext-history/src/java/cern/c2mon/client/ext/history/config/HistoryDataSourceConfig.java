package cern.c2mon.client.ext.history.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class HistoryDataSourceConfig {

  @Bean
  @ConfigurationProperties(prefix = "c2mon.client.history.jdbc")
  public DataSource historyDataSource(Environment environment) {
    String url = environment.getRequiredProperty("c2mon.client.history.jdbc.url");
    String username = environment.getRequiredProperty("c2mon.client.history.jdbc.username");
    String password = environment.getRequiredProperty("c2mon.client.history.jdbc.password");

    String driverClassName = null;
    if (url.contains("hsql")) {
      driverClassName = "org.hsqldb.jdbcDriver";
    } else if (url.contains("oracle")) {
      driverClassName = "oracle.jdbc.OracleDriver";
    } else if (url.contains("mysql")) {
      driverClassName = "com.mysql.jdbc.Driver";
    }

    return DataSourceBuilder.create().driverClassName(driverClassName).url(url).username(username).password(password).build();
  }
}
