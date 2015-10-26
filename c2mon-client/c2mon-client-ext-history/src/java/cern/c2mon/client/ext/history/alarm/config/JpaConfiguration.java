package cern.c2mon.client.ext.history.alarm.config;

import cern.c2mon.client.ext.history.alarm.repository.AlarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Collections;

/**
 * This class is responsible for configuring the {@link javax.persistence.EntityManager} and {@link javax.transaction.TransactionManager} instances that will
 * be used under the hood by our {@link AlarmRepository}.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@EnableJpaRepositories("cern.c2mon.client.ext.history")
public class JpaConfiguration {

  @Autowired
  @Qualifier("historyDataSource")
  private DataSource dataSource;

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
    adapter.setDatabasePlatform("org.hibernate.dialect.Oracle10gDialect");
    adapter.setShowSql(true);
    adapter.setGenerateDdl(false);

    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
    factory.setDataSource(dataSource);
    factory.setPackagesToScan("cern.c2mon.client.ext.history");
    factory.setJpaVendorAdapter(adapter);
    factory.setJpaPropertyMap(Collections.singletonMap("javax.persistence.validation.mode", "none"));
    return factory;
  }

  @Bean
  public JpaTransactionManager transactionManager() {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
    return transactionManager;
  }
}
