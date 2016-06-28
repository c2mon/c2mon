package cern.c2mon.server.cache.dbaccess.config;//package cern.c2mon.server.cache.dbaccess.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import javax.persistence.EntityManagerFactory;
//import javax.sql.DataSource;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author Justin Lewis Salmon
// */
//@Configuration
//@ComponentScan("cern.c2mon.server.cache.dbaccess")
//@EnableJpaRepositories("cern.c2mon.server.cache.dbaccess")
//public class TestConfig {
//
//  @Bean
//  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
//    LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
//    bean.setDataSource(cacheDataSource());
//    bean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
//    bean.setPackagesToScan("cern.c2mon.server.cache.dbaccess");
//
//    Map<String, String> jpaProperties = new HashMap<>();
//    jpaProperties.put("hibernate.hbm2ddl.auto", "create-drop");
//    jpaProperties.put("show-sql", "true");
//    bean.setJpaPropertyMap(jpaProperties);
//
//    return bean;
//  }
//
//  @Bean
//  public DataSource cacheDataSource() {
//    return new EmbeddedDatabaseBuilder().build();
//  }
//
//  @Bean
//  public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
//    return new JpaTransactionManager(emf);
//  }
//}
