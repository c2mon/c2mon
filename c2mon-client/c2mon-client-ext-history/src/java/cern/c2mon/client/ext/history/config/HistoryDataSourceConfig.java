package cern.c2mon.client.ext.history.config;

import org.apache.commons.dbcp.BasicDataSource;
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

    BasicDataSource dataSource = (BasicDataSource) DataSourceBuilder.create().url(url).username(username).password(password).build();

    if (url.contains("hsql")) {
      dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
    }
    else if (url.contains("oracle")) {
      dataSource.setDriverClassName("oracle.jdbc.OracleDriver");

      // In oracle mode, reduce the connection timeout to 5 seconds
      dataSource.addConnectionProperty("oracle.net.CONNECT_TIMEOUT", "50");
    }
    else if (url.contains("mysql")) {
      dataSource.setDriverClassName("com.mysql.jdbc.Driver");
    }

    return dataSource;
  }
}
