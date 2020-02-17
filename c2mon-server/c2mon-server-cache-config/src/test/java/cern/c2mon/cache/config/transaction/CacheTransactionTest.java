package cern.c2mon.cache.config.transaction;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.AbstractCacheTest;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.CacheException;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class CacheTransactionTest extends AbstractCacheTest<Alarm> {
  private static final Logger LOG = LoggerFactory.getLogger(CacheTransactionTest.class);
  private final Alarm nonExistentAlarm = new AlarmCacheObject(1L);
  @Rule
  public Timeout timeoutRule = new Timeout(1, TimeUnit.SECONDS);
  @Inject
  private C2monCache<Alarm> alarmCacheRef;

  @Override
  protected C2monCache<Alarm> getCache() {
    return alarmCacheRef;
  }

  @Override
  protected Alarm getSample() {
    throw new UnsupportedOperationException("This method shouldn't be called in this test");
  }

  @Test(expected = CacheException.class)
  public void exceptionsPropagateFromTransactions() {
    cache.executeTransaction(() -> {
      cache.put(nonExistentAlarm.getId(), nonExistentAlarm);
      throw new CacheException("Crash and burn!");
    });
  }

  @Test
  public void createRollback() {
    // Ensure alarm is not in cache
    cache.remove(nonExistentAlarm.getId());

    try {
      cache.executeTransaction(() -> {
        cache.put(nonExistentAlarm.getId(), nonExistentAlarm);
        throw new CacheException("Crash and burn!");
      });
      fail();
    } catch (CacheException ignored) {
      LOG.trace("Throwing here to shut up compiler warnings", ignored);
    }
    assertFalse(cache.containsKey(nonExistentAlarm.getId()));
  }

  @Test
  public void deleteRollBack() {

  }

  @Test
  public void transactionByOtherCache() {

  }

  @Test
  public void attemptedDeadlock() {

  }
}
