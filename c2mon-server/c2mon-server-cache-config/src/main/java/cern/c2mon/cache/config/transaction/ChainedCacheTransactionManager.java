package cern.c2mon.cache.config.transaction;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * A joint transaction manager for data consistency in operations that span cache and DB
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
