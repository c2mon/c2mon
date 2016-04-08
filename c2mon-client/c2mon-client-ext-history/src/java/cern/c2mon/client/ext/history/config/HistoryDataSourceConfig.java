package cern.c2mon.client.ext.history.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class HistoryDataSourceConfig {

  @Bean
  @ConfigurationProperties(prefix = "c2mon.client.history.jdbc")
  public DataSource historyDataSource() {
    return DataSourceBuilder.create().build();
  }
}
