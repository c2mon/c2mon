package cern.c2mon.server.history.config;

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
 * @author Justin Lewis Salmon
 */
@Configuration
public class HistoryDataSourceConfig {

  @Resource
  private Environment environment;

  @Bean
  @ConfigurationProperties("c2mon.server.history.jdbc")
  public DataSource historyDataSource() {
    String url = environment.getProperty("c2mon.server.history.jdbc.url");

    if (url.contains("hsql")) {
      return new HsqlDatabaseBuilder().setUrl(url).addScript(new ClassPathResource("sql/history-schema-hsqldb.sql")).build();
    } else {
      return DataSourceBuilder.create().build();
    }
  }
}
