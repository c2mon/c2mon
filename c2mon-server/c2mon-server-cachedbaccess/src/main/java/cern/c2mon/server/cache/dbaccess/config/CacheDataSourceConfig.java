package cern.c2mon.server.cache.dbaccess.config;

import cern.c2mon.server.common.util.HsqlDatabaseBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.sql.DataSource;

import java.util.Arrays;
import java.util.Properties;

/**
 * @author Justin Lewis Salmon
 */
@EnableTransactionManagement
@MapperScan(value = "cern.c2mon.server.cache.dbaccess", sqlSessionFactoryRef = "cacheSqlSessionFactory")
public class CacheDataSourceConfig {

  @Autowired
  private CacheDbAccessProperties properties;

  @Bean
  @ConfigurationProperties(prefix = "c2mon.server.cachedbaccess.jdbc")
  public DataSource cacheDataSource() {
    String url = properties.getJdbc().getUrl();
    String username = properties.getJdbc().getUsername();
    String password = properties.getJdbc().getPassword();

    // A simple inspection is done on the JDBC URL to deduce whether to create an in-memory
    // in-process database, start a file-based externally visible database or connect to
    // an external database.
    if (url.contains("hsql")) {
      return HsqlDatabaseBuilder.builder()
                 .url(url)
                 .username(username)
                 .password(password)
                 .script(new ClassPathResource("sql/cache-schema-hsqldb.sql"))
                 .build().toDataSource();
    } else {
      return DataSourceBuilder.create().build();
    }
  }

  @Bean
  public DataSourceTransactionManager cacheTransactionManager(DataSource cacheDataSource) {
    return new DataSourceTransactionManager(cacheDataSource);
  }

  @Bean
  public static SqlSessionFactoryBean cacheSqlSessionFactory(DataSource cacheDataSource) throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(cacheDataSource);
    sessionFactory.setDatabaseIdProvider(databaseIdProvider());
    sessionFactory.setTypeHandlersPackage("cern.c2mon.server.cache.dbaccess.type");
    return sessionFactory;
  }

  @Bean
  public static VendorDatabaseIdProvider databaseIdProvider() {
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
