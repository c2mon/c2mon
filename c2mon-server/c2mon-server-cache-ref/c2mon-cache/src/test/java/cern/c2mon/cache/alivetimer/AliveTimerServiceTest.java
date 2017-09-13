package cern.c2mon.cache.alivetimer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.CacheModuleRef;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.alive.AliveTimerCacheObject;
import cern.c2mon.server.common.config.CommonModule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Szymon Halastra
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        CacheModuleRef.class,
        CommonModule.class
}, loader = AnnotationConfigContextLoader.class)
public class AliveTimerServiceTest {

  @Autowired
  private C2monCache<Long, AliveTimer> aliveTimerCacheRef;

  @Autowired
  private AliveTimerService aliveTimerService;

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
  public void updateAliveTimer() throws InterruptedException {
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L);
    aliveTimer.setActive(true);

    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);

    aliveTimerService.start(1L);

    long firstUpdate = aliveTimerCacheRef.get(1L).getLastUpdate();

    Thread.sleep(100);

    aliveTimerService.update(1L);

    assertTrue("Test if AliveTimer is active", aliveTimerCacheRef.get(1L).isActive());
    assertTrue("Test if AliveTimer is updated", aliveTimerCacheRef.get(1L).getLastUpdate() != firstUpdate);
  }

  @Test
  public void checkExpiredAliveTimer() throws InterruptedException {
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L, 2L, "test", 0L, AliveTimer.ALIVE_TYPE_EQUIPMENT, 20);
    aliveTimer.setActive(true);

    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);

    aliveTimerService.update(aliveTimer.getId());

    Thread.sleep(1000);

    boolean hasExpired = aliveTimerService.hasExpired(aliveTimer.getId());

    assertTrue("Test if AliveTimer is expired", hasExpired);
  }

  @Test
  public void checkActiveAliveTimer() {
    AliveTimer aliveTimer = new AliveTimerCacheObject(1L, 2L, "test", 0L, AliveTimer.ALIVE_TYPE_EQUIPMENT, 0);
    aliveTimer.setActive(true);

    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);

    aliveTimerService.update(aliveTimer.getId());

    boolean hasExpired = aliveTimerService.hasExpired(aliveTimer.getId());

    assertTrue("Test if AliveTimer is not expired", hasExpired);
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

    aliveTimerService.startAllTimers();

    Map<Long, AliveTimer> startedAliveTimers = aliveTimerCacheRef.getAll(aliveTimers.keySet());

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
