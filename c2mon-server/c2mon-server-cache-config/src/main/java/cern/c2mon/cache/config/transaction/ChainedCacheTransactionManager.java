package cern.c2mon.cache.config.transaction;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * A joint transaction manager for data consistency in operations that span cache and DB
 *
 * TODO Once Spring 5.3 is out, we should take advantage of configurable timeouts
 *  for faster unit tests. See https://github.com/spring-projects/spring-framework/issues/25052
 *  and {@code AbstractCacheTransactionTest}.
 */
@Named
public class ChainedCacheTransactionManager extends ChainedTransactionManager {

  @Inject
  public ChainedCacheTransactionManager(
    @Qualifier("cacheTransactionManager") PlatformTransactionManager cacheTransactionManager,
    @Qualifier("databaseTransactionManager") PlatformTransactionManager databaseTransactionManager
  ) {
    super(cacheTransactionManager, databaseTransactionManager);
  }
}
