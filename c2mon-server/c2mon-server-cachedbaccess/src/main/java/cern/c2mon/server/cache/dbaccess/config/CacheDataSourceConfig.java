package cern.c2mon.server.cache.dbaccess.config;

import cern.c2mon.server.common.util.HsqlDatabaseBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author Justin Lewis Salmon
 */
@EnableTransactionManagement
@MapperScan({
    "cern.c2mon.server.cache.dbaccess",
    "cern.c2mon.server.common.process",
    "cern.c2mon.server.common.equipment",
    "cern.c2mon.shared.common.datatag",
    "cern.c2mon.server.common.alarm",
    "cern.c2mon.shared.common.metadata"
})
public class CacheDataSourceConfig {

  @Resource
  private Environment environment;

  @Bean
  @ConfigurationProperties(prefix = "c2mon.server.cachedbaccess.jdbc")
  public DataSource cacheDataSource() {
    String url = environment.getProperty("c2mon.server.cachedbaccess.jdbc.url");

    // A simple inspection is done on the JDBC URL to deduce whether to create an in-memory
    // in-process database, start a file-based externally visible database or connect to
    // an external database.
    if (url.contains("hsql")) {
      HsqlDatabaseBuilder builder = new HsqlDatabaseBuilder().setUrl(url).addScript(new ClassPathResource("sql/cache-schema-hsqldb.sql"));

      if (environment.getProperty("c2mon.server.cachedbaccess.insertTestData", Boolean.class)) {
        builder.addScript(new ClassPathResource("sql/demo/cache-data-demo.sql"));
      }

      return builder.build();
    } else {
      return DataSourceBuilder.create().build();
    }
  }

  @Bean
  public DataSourceTransactionManager cacheTransactionManager(DataSource cacheDataSource) {
    return new DataSourceTransactionManager(cacheDataSource);
  }

  @Bean
  public SqlSessionFactoryBean sqlSessionFactory(DataSource cacheDataSource) throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(cacheDataSource);
    sessionFactory.setTypeHandlersPackage("cern.c2mon.server.cache.dbaccess.type");
    return sessionFactory;
  }

  @Bean
  public VendorDatabaseIdProvider databaseIdProvider() {
    VendorDatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
    Properties properties = new Properties();
    properties.putAll(ImmutableMap.of(
        "HSQL", "hsql",
        "Oracle", "oracle",
        "MySQL", "mysql"
    ));
    databaseIdProvider.setProperties(properties);
    return databaseIdProvider;
  }
}
