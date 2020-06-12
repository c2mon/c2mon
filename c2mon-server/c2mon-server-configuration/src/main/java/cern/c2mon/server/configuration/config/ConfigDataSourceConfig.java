package cern.c2mon.server.configuration.config;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import cern.c2mon.server.common.util.HsqlDatabaseBuilder;

/**
 * @author Justin Lewis Salmon
 */
@EnableTransactionManagement
@MapperScan(value = "cern.c2mon.server.configuration.mybatis", sqlSessionFactoryRef = "configSqlSessionFactory")
public class ConfigDataSourceConfig {

  @Bean
  @org.springframework.boot.context.properties.ConfigurationProperties(prefix = "c2mon.server.configuration.jdbc")
  public DataSourceProperties configDataSourceProperties() {
	  return new DataSourceProperties();
  }

  @Bean
  public DataSource configurationDataSource(@Autowired DataSourceProperties configDataSourceProperties) {
	
	String url = configDataSourceProperties.getUrl();

    // A simple inspection is done on the JDBC URL to deduce whether to create an in-memory
    // in-process database, start a file-based externally visible database or connect to
    // an external database.
    if (url.contains("hsql")) {
      String username = configDataSourceProperties.getUsername();
      String password = configDataSourceProperties.getPassword();
        
      return HsqlDatabaseBuilder.builder()
                 .url(url)
                 .username(username)
                 .password(password)
                 .script(new ClassPathResource("sql/config-schema-hsqldb.sql"))
                 .build().toDataSource();
    } else {
    	return configDataSourceProperties.initializeDataSourceBuilder().build();
    }
  }

  @Bean
  public DataSourceTransactionManager configTransactionManager(DataSource configurationDataSource) {
    return new DataSourceTransactionManager(configurationDataSource);
  }

  @Bean
  public static SqlSessionFactoryBean configSqlSessionFactory(DataSource configurationDataSource,
                                                              VendorDatabaseIdProvider databaseIdProvider) throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(configurationDataSource);
    sessionFactory.setTypeHandlersPackage("cern.c2mon.server.configuration.mybatis");
    sessionFactory.setDatabaseIdProvider(databaseIdProvider);
    return sessionFactory;
  }
}
