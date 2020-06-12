package cern.c2mon.server.cache.dbaccess.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.google.common.collect.ImmutableMap;

import cern.c2mon.server.common.util.HsqlDatabaseBuilder;

/**
 * @author Justin Lewis Salmon
 */
@EnableTransactionManagement
@Configuration
@MapperScan(value = "cern.c2mon.server.cache.dbaccess", sqlSessionFactoryRef = "cacheSqlSessionFactory")
public class CacheDataSourceConfig {

  @Bean
  @ConfigurationProperties(prefix = "c2mon.server.cachedbaccess.jdbc")
  public DataSourceProperties cacheDataSourceProperties() {
	  return new DataSourceProperties();
  }

  @Bean
  public DataSource cacheDataSource(@Autowired DataSourceProperties cacheDataSourceProperties) {
    String url = cacheDataSourceProperties.getUrl();

    // A simple inspection is done on the JDBC URL to deduce whether to create an in-memory
    // in-process database, start a file-based externally visible database or connect to
    // an external database.
    if (url.contains("hsql")) {
      String username =  cacheDataSourceProperties.getUsername();
      String password =  cacheDataSourceProperties.getPassword();
      return HsqlDatabaseBuilder.builder().url(url).username(username).password(password)
       .script(new ClassPathResource("sql/cache-schema-hsqldb.sql")).build().toDataSource();
    } else {
       return cacheDataSourceProperties.initializeDataSourceBuilder().build();
    }
  }

  @Bean
  public DataSourceTransactionManager cacheTransactionManager(DataSource cacheDataSource) {
    return new DataSourceTransactionManager(cacheDataSource);
  }

  @Bean
  public static SqlSessionFactoryBean cacheSqlSessionFactory(DataSource cacheDataSource) {
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
