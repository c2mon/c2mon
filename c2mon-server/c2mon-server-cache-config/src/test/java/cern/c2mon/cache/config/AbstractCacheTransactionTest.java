package cern.c2mon.cache.config;

import cern.c2mon.shared.common.Cacheable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.cache.CacheException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Make sure that your cache module has registered at least one {@link org.springframework.transaction.PlatformTransactionManager}
 *
 * @param <CACHEABLE>
 */
public abstract class AbstractCacheTransactionTest<CACHEABLE extends Cacheable> extends AbstractCacheTest<CACHEABLE> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractCacheTransactionTest.class);
  @Rule
  public Timeout timeoutRule = new Timeout(1, TimeUnit.SECONDS);


  @Test(expected = CacheException.class)
  public void exceptionsPropagateFromTransactions() {
    cache.executeTransaction(() -> {
      cache.put(getSample().getId(), getSample());
      throw new CacheException("Crash and burn!");
    });
  }

  @Test
  public void opensTransaction() {
    getCache().executeTransaction(() -> assertNotNull(TransactionAspectSupport.currentTransactionStatus()));

    // Test both versions of the method
    getCache().executeTransaction(() -> {
      assertNotNull(TransactionAspectSupport.currentTransactionStatus());
      return 1;
    });
  }

  @Test
  public void supportsRollback() {
    getCache().remove(getSample().getId());

    getCache().executeTransaction(() -> {
      getCache().put(getSample().getId(), getSample());

      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    });

    assertFalse(getCache().containsKey(getSample().getId()));
  }

  @Test
  public void exceptionsRollback() {
    // Ensure alarm is not in cache
    cache.remove(getSample().getId());

    try {
      cache.executeTransaction(() -> {
        cache.put(getSample().getId(), getSample());
        throw new CacheException("Crash and burn!");
      });
      fail();
    } catch (CacheException ignored) {
      LOG.trace("Throwing here to shut up compiler warnings", ignored);
    }
    assertFalse(cache.containsKey(getSample().getId()));
  }

  @Test
  public void deleteRollBack() {
    // Ensure alarm is in cache
    cache.put(getSample().getId(), getSample());

    try {
      cache.executeTransaction(() -> {
        cache.remove(getSample().getId(), getSample());
        throw new CacheException("Crash and burn!");
      });
      fail();
    } catch (CacheException ignored) {
      LOG.trace("Throwing here to shut up compiler warnings", ignored);
    }
    assertEquals(getSample(), cache.get(getSample().getId()));
  }

  @Test
  public void transactionByOtherCache() {
    // TODO
  }

  @Test
  public void attemptedDeadlock() {
    // TODO
  }
}
