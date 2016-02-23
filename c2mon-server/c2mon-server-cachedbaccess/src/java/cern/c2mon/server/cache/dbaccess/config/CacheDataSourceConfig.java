package cern.c2mon.server.cache.dbaccess.config;

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
 * This class is responsible for configuring the cache persistence DB datasource bean.
 *
 * A simple inspection is done on the JDBC URL to deduce whether to create an in-memory
 * in-process database, start a file-based in-process database or connect to an external
 * database. The former two cases will also cause the DDL scripts and any data scripts
 * to be run.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
public class CacheDataSourceConfig {

  @Resource
  private Environment environment;

  @Bean
  @ConfigurationProperties(prefix = "c2mon.server.cachedbaccess.jdbc")
  public DataSource cacheDataSource() {
    String url = environment.getProperty("c2mon.server.cachedbaccess.jdbc.url");
    if (url == null || url.contains("hsql")) {
      return new EmbeddedDatabaseBuilder().setType(HSQL).setName("cache").addScripts("classpath:resources/sql/cache-schema-generic.sql",
          "classpath:resources/sql/demo/cache-data-demo.sql").build();
    } else {
      return DataSourceBuilder.create().build();
    }
  }
}
