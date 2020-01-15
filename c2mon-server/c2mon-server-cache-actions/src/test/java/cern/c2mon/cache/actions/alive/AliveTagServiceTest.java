package cern.c2mon.cache.actions.alive;

import cern.c2mon.cache.actions.commfault.CommFaultService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.impl.SimpleC2monCache;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.test.cache.AliveTagCacheObjectFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class AliveTagServiceTest {

  private C2monCache<AliveTag> aliveTimerCacheRef;

  private AliveTagService aliveTimerService;

  private AliveTagCacheObjectFactory factory = new AliveTagCacheObjectFactory();

  @Before
  public void init() {
    aliveTimerCacheRef = new SimpleC2monCache<>("alive-timer-cache");
    aliveTimerService = new AliveTagService(aliveTimerCacheRef, new CommFaultService(new SimpleC2monCache<>("cFault")));
  }

  @Test
  public void startAliveTimer() {
    AliveTag aliveTimer = factory.sampleBase();
    aliveTimer.setValue(false);

    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);

    aliveTimerService.start(aliveTimer.getId(), System.currentTimeMillis());

    assertTrue("Test if AliveTimer is started, set as active", aliveTimerCacheRef.get(aliveTimer.getId()).getValue());
    assertTrue("Test if last update is set up", aliveTimerCacheRef.get(aliveTimer.getId()).getLastUpdate() != 0);
  }

  @Test
  public void startTest() {
    AliveTag aliveTimer = factory.sampleBase();
    aliveTimer.setValue(false);

    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);

    aliveTimerService.start(aliveTimer.getId(), System.currentTimeMillis());

    assertTrue("Test if AliveTimer is started, set as active", aliveTimerCacheRef.get(aliveTimer.getId()).getValue());
    assertTrue("Test if last update is set up", aliveTimerCacheRef.get(aliveTimer.getId()).getLastUpdate() != 0);
  }

  @Test
  public void startForcedAliveTimer() throws InterruptedException {
    AliveTag aliveTimer = factory.sampleBase();
    aliveTimer.setValue(true);

    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);

    aliveTimerService.start(aliveTimer.getId(), System.currentTimeMillis());

    long firstUpdate = aliveTimerCacheRef.get(aliveTimer.getId()).getLastUpdate();

    Thread.sleep(100);

    aliveTimerService.startOrUpdateTimestamp(aliveTimer.getId(), System.currentTimeMillis());

    assertTrue("Test if AliveTimer is active", aliveTimerCacheRef.get(aliveTimer.getId()).getValue());
    assertTrue("Test if AliveTimer is updated", aliveTimerCacheRef.get(aliveTimer.getId()).getLastUpdate() != firstUpdate);
  }

  @Test
  public void checkExpiredAliveTimer() throws InterruptedException {
    AliveTag aliveTimer = factory.sampleBase();
    aliveTimer.setAliveInterval(20);
    aliveTimer.setValue(true);

    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);

    aliveTimerService.startOrUpdateTimestamp(aliveTimer.getId(), System.currentTimeMillis());

    Thread.sleep(70L);

    boolean hasExpired = aliveTimerService.hasExpired(aliveTimer.getId());

    assertTrue("Test if AliveTimer is expired", hasExpired);
  }

  @Test
  public void checkActiveAliveTimer() throws InterruptedException {
    AliveTag aliveTimer = factory.sampleBase();
    aliveTimer.setAliveInterval(0);
    aliveTimer.setValue(true);

    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);

    aliveTimerService.startOrUpdateTimestamp(aliveTimer.getId(), System.currentTimeMillis());

    Thread.sleep(10L);

    boolean hasExpired = aliveTimerService.hasExpired(aliveTimer.getId());

    assertTrue("AliveTimer should be expired after 0 seconds", hasExpired);
  }

  @Test
  public void stopAliveTimer() {
    AliveTag aliveTimer = factory.sampleBase();
    aliveTimer.setValue(true);
    aliveTimer.setLastUpdate(System.currentTimeMillis());

    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);

    aliveTimerService.stop(aliveTimer.getId(), System.currentTimeMillis());

    assertFalse("Test if AliveTimer is active", aliveTimerCacheRef.get(aliveTimer.getId()).getValue());
  }

  @Test
  public void startAllAliveTimers() {
    int size = 10;
    Map<Long, AliveTag> aliveTimers = new HashMap<>(size);
    IntStream.range(0, size).forEach(i -> {
      AliveTag aliveTimer = factory.withCustomId(i);
      aliveTimer.setValue(false);
      aliveTimers.put(aliveTimer.getId(), aliveTimer);
    });

    aliveTimerCacheRef.putAll(aliveTimers);

    aliveTimerService.startAllInactiveTimers();

    Map<Long, AliveTag> startedAliveTimers = aliveTimerCacheRef.getAll(aliveTimers.keySet());

    List<Boolean> actualActive = startedAliveTimers.values().stream().map(AliveTag::getValue).collect(Collectors.toList());
    List<Boolean> expectedTrue = new ArrayList<>(Collections.nCopies(size, Boolean.TRUE));

    List<Long> actualLastUpdates = startedAliveTimers.values().stream().map(AliveTag::getLastUpdate).collect(Collectors.toList());
    List<Long> expectedZeros = new ArrayList<>(Collections.nCopies(size, 0L));

    assertEquals("All AliveTimers should have active status", expectedTrue, actualActive);
    assertNotEquals("All AliveTimers should have last updated different than 0", expectedZeros, actualLastUpdates);
  }

  @Test
  public void stopAllAliveTimers() {
    int size = 10;
    Map<Long, AliveTag> aliveTimers = new HashMap<>(size);
    IntStream.range(0, size).forEach(i -> {
      AliveTag aliveTimer = factory.withCustomId(i);
      aliveTimer.setValue(true);
      aliveTimer.setLastUpdate(System.currentTimeMillis());
      aliveTimers.put(aliveTimer.getId(), aliveTimer);
    });

    aliveTimerCacheRef.putAll(aliveTimers);
    aliveTimerService.stopAllActiveTimers();

    Map<Long, AliveTag> stoppedAliveTimers = aliveTimerCacheRef.getAll(aliveTimers.keySet());
    List<Boolean> actualNotActive = stoppedAliveTimers.values().stream().map(AliveTag::getValue).collect(Collectors.toList());
    List<Boolean> expectedFalse = new ArrayList<>(Collections.nCopies(size, Boolean.FALSE));

    List<Long> actualLastUpdates = stoppedAliveTimers.values().stream().map(AliveTag::getLastUpdate).collect(Collectors.toList());
    List<Long> expectedZeros = new ArrayList<>(Collections.nCopies(size, 0L));

    assertEquals("All AliveTimers should have active status", expectedFalse, actualNotActive);
    assertNotEquals("All AliveTimers should have last updated different than 0", expectedZeros, actualLastUpdates);
  }

  @Test
  public void aliveTimerOpsAreSideeffects() {
    // Test to make sure stuff like the peek, works properly for the current code

  }
}
