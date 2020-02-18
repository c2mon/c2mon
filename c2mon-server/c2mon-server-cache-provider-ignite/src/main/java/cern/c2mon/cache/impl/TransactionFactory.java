package cern.c2mon.cache.impl;

import cern.c2mon.server.common.util.KotlinAPIs;
import org.apache.ignite.transactions.spring.SpringTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransactionFactory {

  @Bean
  public PlatformTransactionManager txManager(IgniteC2monBean igniteC2monBean) {
    return KotlinAPIs.apply(new SpringTransactionManager(), tManager ->
      tManager.setIgniteInstanceName(igniteC2monBean.name())
    );
  }

}
