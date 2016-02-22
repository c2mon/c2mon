package cern.c2mon.server.shorttermlog.config;

import cern.c2mon.server.test.HsqlServer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.annotation.Resource;
import javax.sql.DataSource;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;

/**
 * This class is responsible for configuring the short-term log (STL) datasource bean.
 *
 * A simple inspection is done on the JDBC URL to deduce whether to create an in-memory
 * in-process database, start a file-based in-process database or connect to an external
 * database. The former two cases will also cause the DDL scripts and any data scripts
 * to be run.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
public class StlDataSourceConfig {

  @Resource
  private Environment environment;

  @Bean
  @ConfigurationProperties("datasource.stl")
  public DataSource stlDataSource() {
    String url = environment.getProperty("datasource.stl.url");
    if (url == null || url.contains("hsqldb:mem")) {
      return new EmbeddedDatabaseBuilder().setType(HSQL).setName("stl").addScript("classpath:resources/sql/stl-schema-generic.sql").build();
    } else if (url.contains("hsql://")) {
      HsqlServer hsqlServer = new HsqlServer("file:///tmp/stl", "stl");
      hsqlServer.start();

      DataSource dataSource = DataSourceBuilder.create()
          .url(environment.getRequiredProperty("datasource.stl.url"))
          .username(environment.getRequiredProperty("datasource.stl.username"))
          .password(environment.getRequiredProperty("datasource.stl.password")).build();

      DatabasePopulatorUtils.execute(new ResourceDatabasePopulator(new ClassPathResource("resources/sql/stl-schema-generic.sql")), dataSource);
      return dataSource;
    } else {
      return DataSourceBuilder.create().build();
    }
  }
}
