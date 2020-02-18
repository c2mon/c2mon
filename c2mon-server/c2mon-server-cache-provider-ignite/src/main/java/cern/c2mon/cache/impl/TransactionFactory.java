package cern.c2mon.cache.impl;

import org.apache.ignite.transactions.spring.SpringTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransactionFactory {

  @Bean(name = "cacheTransactionManager")
  public PlatformTransactionManager cacheTransactionManager(IgniteC2monBean igniteC2monBean) {
    SpringTransactionManager transactionManager = new SpringTransactionManager();
    transactionManager.setIgniteInstanceName(igniteC2monBean.name());
    return transactionManager;
  }

}
