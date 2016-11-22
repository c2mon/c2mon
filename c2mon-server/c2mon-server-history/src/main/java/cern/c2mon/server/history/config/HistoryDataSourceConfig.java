package cern.c2mon.server.history.config;

import cern.c2mon.server.common.util.HsqlDatabaseBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@MapperScan(
    value = "cern.c2mon.server.history.mapper",
    sqlSessionFactoryRef = "historySqlSessionFactory"
)
@Import({
    TagHistoryConfig.class,
    AlarmHistoryConfig.class,
    CommandHistoryConfig.class
})
public class HistoryDataSourceConfig implements EnvironmentAware {

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

  @Bean
  public DataSourceTransactionManager historyTransactionManager(DataSource historyDataSource) {
    return new DataSourceTransactionManager(historyDataSource);
  }

  @Bean
  public SqlSessionFactoryBean historySqlSessionFactory(DataSource historyDataSource) throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(historyDataSource);
    sessionFactory.setTypeHandlersPackage("cern.c2mon.server.history.mapper");

    VendorDatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
    Properties properties = new Properties();
    properties.putAll(ImmutableMap.of(
        "HSQL", "oracle",
        "Oracle", "oracle",
        "MySQL", "mysql"
    ));
    databaseIdProvider.setProperties(properties);

    sessionFactory.setDatabaseIdProvider(databaseIdProvider);
    return sessionFactory;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }
}
