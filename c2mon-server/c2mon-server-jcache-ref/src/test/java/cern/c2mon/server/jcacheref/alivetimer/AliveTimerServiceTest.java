package cern.c2mon.server.jcacheref.alivetimer;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.alive.AliveTimerCacheObject;
import cern.c2mon.server.jcacheref.HazelcastBaseTestingSetup;
import cern.c2mon.server.jcacheref.prototype.alive.AliveTimerCacheService;

import static org.junit.Assert.assertTrue;

/**
 * @author Szymon Halastra
 */
public class AliveTimerServiceTest extends HazelcastBaseTestingSetup {

  Cache<Long, AliveTimer> aliveTimerCache;
  AliveTimerCacheService aliveTimerCacheService;

  @Before
  public void setup() {
    CachingProvider provider = Caching.getCachingProvider();
    CacheManager cacheManager = provider.getCacheManager();

    aliveTimerCache = cacheManager.getCache("aliveTimerCache", Long.class, AliveTimer.class);

    aliveTimerCacheService = new AliveTimerCacheService(aliveTimerCache);
  }

  @Test
  public void startAliveTimer() {
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L);
    aliveTimer.setActive(false);

    aliveTimerCache.put(aliveTimer.getId(), aliveTimer);

    aliveTimerCacheService.start(1L);

    assertTrue("Test if AliveTimer is started, set as active", aliveTimerCache.get(1L).isActive());
    assertTrue("Test if last update is set up", aliveTimerCache.get(1L).getLastUpdate() != 0);
  }

  @Test
  public void updateAliveTimer() throws InterruptedException {
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L);
    aliveTimer.setActive(true);

    aliveTimerCache.put(aliveTimer.getId(), aliveTimer);

    aliveTimerCacheService.start(1L);

    long firstUpdate = aliveTimerCache.get(1L).getLastUpdate();

    Thread.sleep(100);

    aliveTimerCacheService.update(1L);

    assertTrue("Test if AliveTimer is active", aliveTimerCache.get(1L).isActive());
    assertTrue("Test if AliveTimer is updated", aliveTimerCache.get(1L).getLastUpdate() != firstUpdate);
  }

  @Test
  public void checkExpiredAliveTimer() throws InterruptedException {
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L,2L, "test", 0L, AliveTimer.ALIVE_TYPE_EQUIPMENT, 20);
    aliveTimer.setActive(true);

    aliveTimerCache.put(aliveTimer.getId(), aliveTimer);

    aliveTimerCacheService.update(aliveTimer.getId());

    Thread.sleep(1000);

    boolean hasExpired = aliveTimerCacheService.hasExpired(aliveTimer.getId());

    assertTrue("Test if AliveTimer is expired", hasExpired);
  }

  @Test
  public void checkActiveAliveTimer() {
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L,2L, "test", 0L, AliveTimer.ALIVE_TYPE_EQUIPMENT, 0);
    aliveTimer.setActive(true);

    aliveTimerCache.put(aliveTimer.getId(), aliveTimer);

    aliveTimerCacheService.update(aliveTimer.getId());

    boolean hasExpired = aliveTimerCacheService.hasExpired(aliveTimer.getId());

    assertTrue("Test if AliveTimer is not expired", hasExpired);
  }

  @Test
  public void stopAliveTimer() {

  }

  @Test
  public void startAllAliveTimers() {

  }

  @Test
  public void stopAllAliveTimers() {

  }
}
