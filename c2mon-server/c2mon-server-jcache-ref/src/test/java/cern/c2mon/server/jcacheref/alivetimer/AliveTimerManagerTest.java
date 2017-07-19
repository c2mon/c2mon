package cern.c2mon.server.jcacheref.alivetimer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.cache.Cache;
import javax.cache.Caching;

import org.apache.ignite.IgniteCache;
import org.easymock.EasyMock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.alive.AliveTimerCacheObject;
import cern.c2mon.server.jcacheref.prototype.alive.AliveTimerCacheRef;
import cern.c2mon.server.jcacheref.prototype.alive.AliveTimerCacheService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Szymon Halastra
 */

public class AliveTimerManagerTest {

  private static AliveTimerCacheRef aliveTimerCache;

  private static AliveTimerCacheService aliveTimerCacheService;

  @BeforeClass
  public static void setup() {
//    aliveTimerCache = EasyMock.mock(A.class);

    aliveTimerCacheService = new AliveTimerCacheService(aliveTimerCache);
  }

  @AfterClass
  public static void cleanup() {
    Caching.getCachingProvider().getCacheManager().destroyCache("aliveTimerCache");
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
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L, 2L, "test", 0L, AliveTimer.ALIVE_TYPE_EQUIPMENT, 20);
    aliveTimer.setActive(true);

    aliveTimerCache.put(aliveTimer.getId(), aliveTimer);

    aliveTimerCacheService.update(aliveTimer.getId());

    Thread.sleep(1000);

    boolean hasExpired = aliveTimerCacheService.hasExpired(aliveTimer.getId());

    assertTrue("Test if AliveTimer is expired", hasExpired);
  }

  @Test
  public void checkActiveAliveTimer() {
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L, 2L, "test", 0L, AliveTimer.ALIVE_TYPE_EQUIPMENT, 0);
    aliveTimer.setActive(true);

    aliveTimerCache.put(aliveTimer.getId(), aliveTimer);

    aliveTimerCacheService.update(aliveTimer.getId());

    boolean hasExpired = aliveTimerCacheService.hasExpired(aliveTimer.getId());

    assertTrue("Test if AliveTimer is not expired", hasExpired);
  }

  @Test
  public void stopAliveTimer() {
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L);
    aliveTimer.setActive(true);
    aliveTimer.setLastUpdate(System.currentTimeMillis());

    aliveTimerCache.put(aliveTimer.getId(), aliveTimer);

    aliveTimerCacheService.stop(1L);

    assertFalse("Test if AliveTimer is active", aliveTimerCache.get(1L).isActive());
  }

  @Test
  public void startAllAliveTimers() {
    int size = 10;
    Map<Long, AliveTimer> aliveTimers = new HashMap<>(size);
    IntStream.range(0, size).forEach(i -> {
      AliveTimer aliveTimer = new AliveTimerCacheObject((long) i);
      aliveTimer.setActive(false);
      aliveTimers.put(aliveTimer.getId(), aliveTimer);
    });

    aliveTimerCache.putAll(aliveTimers);

    aliveTimerCacheService.startAllTimers();

    Map<Long, AliveTimer> startedAliveTimers = aliveTimerCache.getAll(aliveTimers.keySet());

    List<Boolean> actualActive = startedAliveTimers.values().stream().map(AliveTimer::isActive).collect(Collectors.toList());
    List<Boolean> expectedTrue = new ArrayList<>(Collections.nCopies(size, Boolean.TRUE));

    List<Long> actualLastUpdates = startedAliveTimers.values().stream().map(AliveTimer::getLastUpdate).collect(Collectors.toList());
    List<Long> expectedZeros = new ArrayList<>(Collections.nCopies(size, 0L));

    assertTrue("All AliveTimers should have active status", expectedTrue.equals(actualActive));
    assertFalse("All AliveTimers should have last updated different than 0", expectedZeros.equals(actualLastUpdates));
  }

  @Test
  public void stopAllAliveTimers() {

  }
}
