package cern.c2mon.cache.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.shared.common.Cacheable;
import org.apache.ignite.transactions.TransactionDeadlockException;
import org.apache.ignite.transactions.TransactionTimeoutException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.cache.CacheException;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.Assert.*;

/**
 * Make sure that your cache module has registered at least one {@link PlatformTransactionManager}
 *
 * @param <CACHEABLE>
 */
public abstract class AbstractCacheTransactionTest<CACHEABLE extends Cacheable> extends AbstractCacheTest<CACHEABLE> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractCacheTransactionTest.class);

  @Rule
  public Timeout timeoutRule = new Timeout(7, TimeUnit.SECONDS);

  @Test(expected = CacheException.class)
  public void exceptionsPropagateFromTransactions() {
    cache.executeTransaction(() -> {
      cache.put(getSample().getId(), getSample());
      throw new CacheException("Crash and burn!");
    });
  }

  @Test
  public void opensTransaction() {
    cache.executeTransaction(() -> assertNotNull(TransactionAspectSupport.currentTransactionStatus()));

    // Test both versions of the method
    cache.executeTransaction(() -> {
      assertNotNull(TransactionAspectSupport.currentTransactionStatus());
      return 1;
    });
  }

  @Test
  public void supportsRollback() {
    cache.remove(getSample().getId());
    assertFalse(cache.containsKey(getSample().getId()));

    cache.executeTransaction(() -> {
      cache.put(getSample().getId(), getSample());
      assertTrue(cache.containsKey(getSample().getId()));

      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    });

    assertFalse(cache.containsKey(getSample().getId()));
  }

  @Test
  public void exceptionsRollback() {
    // Ensure alarm is not in cache
    cache.remove(getSample().getId());
    assertFalse(cache.containsKey(getSample().getId()));

    try {
      cache.executeTransaction(() -> {
        cache.put(getSample().getId(), getSample());
        assertTrue(cache.containsKey(getSample().getId()));
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
    assertTrue(cache.containsKey(getSample().getId()));

    try {
      cache.executeTransaction(() -> {
        cache.remove(getSample().getId(), getSample());
        assertFalse(cache.containsKey(getSample().getId()));
        throw new CacheException("Crash and burn!");
      });
      fail();
    } catch (CacheException ignored) {
      LOG.trace("Throwing here to shut up compiler warnings", ignored);
    }

    assertEquals(getSample(), cache.get(getSample().getId()));
  }

  @Test
  public void deadlock() {
    cache.put(3333L, getSample());
    cache.put(6666L, getSample());

    Future f1 = runInThread(() -> cache.executeTransaction(() -> {
      cache.put(3333L, getSample());
      cache.put(6666L, getSample());
    }));

    Future f2 = runInThread(() -> cache.executeTransaction(() -> {
      cache.put(6666L, getSample());
      cache.put(3333L, getSample());
    }));

    assertDeadlock(f1, f2);
  }

  /**
   * Picking any other concrete cache implementation to test against every abstract
   * cache instance for issues related to transaction concurrency (e.g. deadlocks).
   */
  @Inject
  private C2monCache<Alarm> arbitraryConcreteCache;

  @Test
  public void deadlockTransactionByOtherCache() {
    cache.put(4444L, getSample());
    arbitraryConcreteCache.put(8888L, new AlarmCacheObject(0L));

    Future f1 = runInThread(() -> cache.executeTransaction(() -> {
      cache.put(4444L, getSample());
      arbitraryConcreteCache.put(8888L, new AlarmCacheObject(0L));
    }));

    Future f2 = runInThread(() -> cache.executeTransaction(() -> {
      arbitraryConcreteCache.put(8888L, new AlarmCacheObject(0L));
      cache.put(4444L, getSample());
    }));

    assertDeadlock(f1, f2);
  }

  private Future runInThread(Runnable r) {
    ExecutorService executor = newSingleThreadExecutor();
    return executor.submit(r);
  }

  /**
   * See https://apacheignite.readme.io/docs/transactions#deadlock-detection.
   */
  private void assertDeadlock(Future f1, Future f2) {
    List<Exception> exceptions = new ArrayList<>();

    try {
      f1.get(6, TimeUnit.SECONDS);
      fail("No deadlock detected (thread 1 completed)");
    } catch (TimeoutException ignored) {
      fail("No deadlock detected (thread 1 timed out)");
    } catch (InterruptedException | ExecutionException e) {
      exceptions.add(e);
    }

    try {
      f2.get(100, TimeUnit.MILLISECONDS);
      fail("No deadlock detected (thread 2 completed)");
    } catch (TimeoutException ignored) {
      fail("No deadlock detected (thread 2 timed out)");
    } catch (InterruptedException | ExecutionException e) {
      exceptions.add(e);
    }

    assertEquals(2, exceptions.size());
    exceptions.forEach(e -> {
      assertTrue(e.getCause() instanceof CacheException);
      assertTrue(e.getCause().getCause() instanceof TransactionTimeoutException);
    });
    assertTrue(
      exceptions.stream().filter(e -> e.getCause().getCause().getCause() instanceof TransactionDeadlockException).count() >= 1
    );
  }
}
