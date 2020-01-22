package cern.c2mon.cache;

import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.actions.supervision.SupervisedCacheService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.common.equipment.AbstractSupervisedCacheObject;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.server.test.CachePopulationRule;
import cern.c2mon.server.test.SupervisionCacheResetRule;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static cern.c2mon.shared.common.supervision.SupervisionStatus.*;
import static org.junit.Assert.*;

@ContextConfiguration(classes = {
  SupervisionCacheResetRule.class,
  CachePopulationRule.class
})
public abstract class SupervisedServiceTest<T extends Supervised, T_IMPL extends AbstractSupervisedCacheObject>
  extends AbstractCacheTest<T, T_IMPL> {

  @Inject
  SupervisionStateTagService stateTagService;

  @Inject
  C2monCache<CommFaultTag> commFaultTagCache;

  @Inject
  C2monCache<AliveTag> aliveTagCache;

  @Inject
  @Rule
  public SupervisionCacheResetRule supervisionCacheResetRule;

  private SupervisedCacheService<T> supervisedService;
  private T sample;

  protected abstract SupervisedCacheService<T> getSupervisedService();

  @Before
  public void preloadCaches() {
    cache = initCache();
//    cache.setCacheListenerManager(new CacheListenerManagerImpl<>());
    supervisedService = getSupervisedService();
    sample = getSample();
  }

  @Test
  public void getSupervisionStatus() {
    verifySupervisionEvent(DOWN);
  }

  @Test
  public void start() throws InterruptedException {
    cacheSupervision(
      () -> supervisedService.start(sample.getId(), System.currentTimeMillis()),
      STARTUP);
  }

  @Test
  public void stop() throws InterruptedException {
    setAsRunning();
    cacheSupervision(
      () -> supervisedService.stop(sample.getId(), System.currentTimeMillis()),
      DOWN);
  }

  @Test
  public void suspend() throws InterruptedException {
    setAsRunning();
    cacheSupervision(
      () -> supervisedService.suspend(sample.getId(), System.currentTimeMillis(), ""),
      DOWN);
  }

  @Test
  public void resume() throws InterruptedException {
    cacheSupervision(
      () -> supervisedService.resume(sample.getId(), System.currentTimeMillis(), ""),
      RUNNING);
  }

  @Test
  public void resumingStartedObjectChangesTimestamp() throws InterruptedException {
    cache.put(sample.getId(), sample);

    supervisedService.start(sample.getId(), System.currentTimeMillis() - 1);
    long initialTime = stateTagService.getSupervisionEvent(sample.getStateTagId()).getEventTime().getTime();

    supervisedService.resume(sample.getId(), System.currentTimeMillis(), "");

    long resumedTime = stateTagService.getSupervisionEvent(sample.getStateTagId()).getEventTime().getTime();

    assertNotEquals(initialTime, resumedTime);
  }

  @Test
  public void resumingObjectTwiceResultsInNoEffect() {
    cache.put(sample.getId(), sample);

    long initialTimeMillis = System.currentTimeMillis() - 1;
    supervisedService.start(sample.getId(), initialTimeMillis);

    long timeOfRunningNotStartupStatus = initialTimeMillis + 1;
    supervisedService.resume(sample.getId(), timeOfRunningNotStartupStatus, "");
    long resumedTime = stateTagService.getSupervisionEvent(sample.getStateTagId()).getEventTime().getTime();

    // This should have no change
    supervisedService.resume(sample.getId(), initialTimeMillis + 2, "");
    long secondResumedTime = stateTagService.getSupervisionEvent(sample.getStateTagId()).getEventTime().getTime();
    assertEquals(resumedTime, secondResumedTime);
  }

  private void verifySupervisionEvent(SupervisionStatus expectedStatus) {
    SupervisionEvent event = stateTagService.getSupervisionEvent(sample.getStateTagId());

    assertEquals(sample.getId(), event.getEntityId());
    assertEquals(event.getEntity(), sample.getSupervisionEntity());
    assertEquals(expectedStatus, event.getStatus());

    // Repeating the attempt yields an equal result
    assertEquals(event, stateTagService.getSupervisionEvent(sample.getStateTagId()));
  }

  private void cacheSupervision(Runnable cacheAction, SupervisionStatus expected) throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    stateTagService.getCache().getCacheListenerManager().registerListener(i -> latch.countDown(), CacheEvent.UPDATE_ACCEPTED);
    cache.put(sample.getId(), sample);

    cacheAction.run();

    assertTrue(latch.await(150, TimeUnit.MILLISECONDS));

    // Cache object has achieved expected status
    verifySupervisionEvent(expected);
  }

  private void setAsRunning() {
    if (sample.getAliveTagId() != null)
      aliveTagCache.computeQuiet(sample.getAliveTagId(), aliveTag -> aliveTag.setValue(true));
    if (sample instanceof AbstractEquipment && ((AbstractEquipment) sample).getCommFaultTagId() != null)
      commFaultTagCache.computeQuiet(((AbstractEquipment) sample).getCommFaultTagId(), commFaultTag -> commFaultTag.setValue(true));

    // Should always have a state tag
    stateTagService.getCache().computeQuiet(sample.getStateTagId(), stateTag -> stateTag.setSupervision(
      RUNNING, "", new Timestamp(1L)
    ));
  }
}
