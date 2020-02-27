package cern.c2mon.server.history.config;

import cern.c2mon.server.common.util.HsqlDatabaseBuilder;
import cern.c2mon.server.common.util.KotlinAPIs;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.inject.Inject;
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
public class HistoryDataSourceConfig {

  @Inject
  private HistoryProperties properties;

  @Bean
  @ConfigurationProperties("c2mon.server.history.jdbc")
  public DataSource historyDataSource() {
    String url = properties.getJdbc().getUrl();
    String username = properties.getJdbc().getUsername();
    String password = properties.getJdbc().getPassword();

    if (url.contains("hsql")) {
      return HsqlDatabaseBuilder.builder()
                 .url(url)
                 .username(username)
                 .password(password)
                 .script(new ClassPathResource("sql/history-schema-hsqldb.sql")).build()
                 .toDataSource();
    } else {
      return DataSourceBuilder.create().build();
    }
  }

  @Bean
  public DataSourceTransactionManager historyTransactionManager(DataSource historyDataSource) {
    return new DataSourceTransactionManager(historyDataSource);
  }

  @Bean
  public static SqlSessionFactoryBean historySqlSessionFactory(DataSource historyDataSource) {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(historyDataSource);
    sessionFactory.setTypeHandlersPackage("cern.c2mon.server.history.mapper");

    VendorDatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();

    databaseIdProvider.setProperties(
      KotlinAPIs.apply(new Properties(), properties -> {
        properties.setProperty("HSQL", "hsql");
        properties.setProperty("Oracle", "oracle");
        properties.setProperty("MySQL", "mysql");
      })
    );

    sessionFactory.setDatabaseIdProvider(databaseIdProvider);
    return sessionFactory;
  }
}
