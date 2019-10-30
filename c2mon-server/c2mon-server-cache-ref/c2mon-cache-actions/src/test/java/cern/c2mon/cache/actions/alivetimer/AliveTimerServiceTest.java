package cern.c2mon.cache.actions.alivetimer;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.impl.SimpleC2monCache;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.alive.AliveTimerCacheObject;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class AliveTimerServiceTest {

  private C2monCache<AliveTimer> aliveTimerCacheRef;

  private AliveTimerService aliveTimerService;

  @Before
  public void init() {
    aliveTimerCacheRef = new SimpleC2monCache<>("alive-timer-cache");
    aliveTimerService = new AliveTimerService(aliveTimerCacheRef);
  }

  @Test
  public void startAliveTimer() {
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L);
    aliveTimer.setActive(false);

    AliveTimer startedAliveTimer = new AliveTimerCacheObject(1L);
    startedAliveTimer.setActive(true);
    startedAliveTimer.setLastUpdate(System.currentTimeMillis());

    aliveTimerCacheRef.put(1L, aliveTimer);

    aliveTimerService.start(1L);

    assertTrue("Test if AliveTimer is started, set as active", aliveTimerCacheRef.get(1L).isActive());
    assertTrue("Test if last update is set up", aliveTimerCacheRef.get(1L).getLastUpdate() != 0);
  }

  @Test
  public void startTest() {
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L);
    aliveTimer.setActive(false);

    aliveTimerCacheRef.put(1L, aliveTimer);

    aliveTimerService.start(1L);

    assertTrue("Test if AliveTimer is started, set as active", aliveTimerCacheRef.get(1L).isActive());
    assertTrue("Test if last update is set up", aliveTimerCacheRef.get(1L).getLastUpdate() != 0);
  }

  @Test
  public void startForcedAliveTimer() throws InterruptedException {
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L);
    aliveTimer.setActive(true);

    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);

    aliveTimerService.start(1L);

    long firstUpdate = aliveTimerCacheRef.get(1L).getLastUpdate();

    Thread.sleep(100);

    aliveTimerService.startOrUpdateTimestamp(1L);

    assertTrue("Test if AliveTimer is active", aliveTimerCacheRef.get(1L).isActive());
    assertTrue("Test if AliveTimer is updated", aliveTimerCacheRef.get(1L).getLastUpdate() != firstUpdate);
  }

  @Test
  public void checkExpiredAliveTimer() throws InterruptedException {
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L, 2L, "test", 0L, AliveTimer.ALIVE_TYPE_EQUIPMENT, 20);
    aliveTimer.setActive(true);

    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);

    aliveTimerService.startOrUpdateTimestamp(aliveTimer.getId());

    Thread.sleep(70L);

    boolean hasExpired = aliveTimerService.hasExpired(aliveTimer.getId());

    assertTrue("Test if AliveTimer is expired", hasExpired);
  }

  @Test
  public void checkActiveAliveTimer() throws InterruptedException {
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L, 2L, "test", 0L, AliveTimer.ALIVE_TYPE_EQUIPMENT, 0);
    aliveTimer.setActive(true);

    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);

    aliveTimerService.startOrUpdateTimestamp(aliveTimer.getId());

    Thread.sleep(10L);

    boolean hasExpired = aliveTimerService.hasExpired(aliveTimer.getId());

    assertTrue("AliveTimer should be expired after 0 seconds", hasExpired);
  }

  @Test
  public void stopAliveTimer() {
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L);
    aliveTimer.setActive(true);
    aliveTimer.setLastUpdate(System.currentTimeMillis());

    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);

    aliveTimerService.stop(1L);

    assertFalse("Test if AliveTimer is active", aliveTimerCacheRef.get(1L).isActive());
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

    aliveTimerCacheRef.putAll(aliveTimers);

    aliveTimerService.startAllInactiveTimers();

    Map<Long, AliveTimer> startedAliveTimers = aliveTimerCacheRef.getAll(aliveTimers.keySet());

    List<Boolean> actualActive = startedAliveTimers.values().stream().map(AliveTimer::isActive).collect(Collectors.toList());
    List<Boolean> expectedTrue = new ArrayList<>(Collections.nCopies(size, Boolean.TRUE));

    List<Long> actualLastUpdates = startedAliveTimers.values().stream().map(AliveTimer::getLastUpdate).collect(Collectors.toList());
    List<Long> expectedZeros = new ArrayList<>(Collections.nCopies(size, 0L));

    assertEquals("All AliveTimers should have active status", expectedTrue, actualActive);
    assertNotEquals("All AliveTimers should have last updated different than 0", expectedZeros, actualLastUpdates);
  }

  @Test
  public void stopAllAliveTimers() {
    int size = 10;
    Map<Long, AliveTimer> aliveTimers = new HashMap<>(size);
    IntStream.range(0, size).forEach(i -> {
      AliveTimer aliveTimer = new AliveTimerCacheObject((long) i);
      aliveTimer.setActive(true);
      aliveTimer.setLastUpdate(System.currentTimeMillis());
      aliveTimers.put(aliveTimer.getId(), aliveTimer);
    });

    aliveTimerCacheRef.putAll(aliveTimers);
    aliveTimerService.stopAllActiveTimers();

    Map<Long, AliveTimer> stoppedAliveTimers = aliveTimerCacheRef.getAll(aliveTimers.keySet());
    List<Boolean> actualNotActive = stoppedAliveTimers.values().stream().map(AliveTimer::isActive).collect(Collectors.toList());
    List<Boolean> expectedFalse = new ArrayList<>(Collections.nCopies(size, Boolean.FALSE));

    List<Long> actualLastUpdates = stoppedAliveTimers.values().stream().map(AliveTimer::getLastUpdate).collect(Collectors.toList());
    List<Long> expectedZeros = new ArrayList<>(Collections.nCopies(size, 0L));

    assertEquals("All AliveTimers should have active status", expectedFalse, actualNotActive);
    assertNotEquals("All AliveTimers should have last updated different than 0", expectedZeros, actualLastUpdates);
  }

  @Test
  public void aliveTimerOpsAreSideeffects() {
    // Test to make sure stuff like the peek, works properly for the current code

  }
}
