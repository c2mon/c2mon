package cern.c2mon.server.configuration.config;

import cern.c2mon.server.common.util.HsqlDatabaseBuilder;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @author Justin Lewis Salmon
 */
@EnableTransactionManagement
@MapperScan(value = "cern.c2mon.server.configuration.mybatis", sqlSessionFactoryRef = "configSqlSessionFactory")
public class ConfigDataSourceConfig implements EnvironmentAware {

  // We use {@link EnvironmentAware} here to ensure that Mybatis doesn't
  // start scanning for mappers too early.
  private Environment environment;

  @Autowired
  private ConfigurationProperties properties;

  @Bean
  @org.springframework.boot.context.properties.ConfigurationProperties(prefix = "c2mon.server.configuration.jdbc")
  public DataSource configurationDataSource() {
    String url = properties.getJdbc().getUrl();

    // A simple inspection is done on the JDBC URL to deduce whether to create an in-memory
    // in-process database, start a file-based externally visible database or connect to
    // an external database.
    if (url.contains("hsql")) {
      return new HsqlDatabaseBuilder().setUrl(url).addScript(new ClassPathResource("sql/config-schema-hsqldb.sql")).build();
    } else {
      return DataSourceBuilder.create().build();
    }
  }

  @Bean
  public DataSourceTransactionManager configTransactionManager(DataSource configurationDataSource) {
    return new DataSourceTransactionManager(configurationDataSource);
  }

  @Bean
  public SqlSessionFactoryBean configSqlSessionFactory(DataSource configurationDataSource, VendorDatabaseIdProvider databaseIdProvider) throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(configurationDataSource);
    sessionFactory.setTypeHandlersPackage("cern.c2mon.server.configuration.mybatis");
    sessionFactory.setDatabaseIdProvider(databaseIdProvider);
    return sessionFactory;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }
}
