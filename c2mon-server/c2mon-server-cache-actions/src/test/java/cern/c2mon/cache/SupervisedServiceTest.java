package cern.c2mon.cache;

import cern.c2mon.cache.actions.listener.SupervisedServiceListenerTest;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.equipment.AbstractSupervisedCacheObject;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;

import static cern.c2mon.shared.common.supervision.SupervisionStatus.*;
import static org.junit.Assert.assertEquals;

public abstract class SupervisedServiceTest<T extends Supervised, T_IMPL extends AbstractSupervisedCacheObject>
  extends SupervisedServiceListenerTest<T, T_IMPL> {

  @Inject
  SupervisionStateTagService stateTagService;

  @Inject
  C2monCache<AliveTag> aliveTagCache;

  @Before
  public void preloadStateTags() {
    aliveTagCache.init();
    stateTagService.getCache().init();
  }

  @Test
  public void getSupervisionStatus() {
    cache.put(sample.getId(), sample);

    verifySupervisionEvent(DOWN);
  }

  @Test
  @Ignore
  public void start() throws InterruptedException {
    cacheSupervision(
      () -> supervisedService.start(sample.getId(), Timestamp.from(Instant.now())),
      STARTUP);
  }

  @Test
  @Ignore
  public void stop() throws InterruptedException {
    cacheSupervision(
      () -> supervisedService.stop(sample.getId(), Timestamp.from(Instant.now())),
      DOWN);
  }

  @Test
  @Ignore
  public void suspend() throws InterruptedException {
    cacheSupervision(
      () -> supervisedService.suspend(sample.getId(), Timestamp.from(Instant.now()), ""),
      DOWN);
  }

  @Test
  @Ignore
  public void resume() throws InterruptedException {
    cacheSupervision(
      () -> supervisedService.resume(sample.getId(), Timestamp.from(Instant.now()), ""),
      RUNNING);
  }

  @Test
  @Ignore
  public void resumingStartedObjectResultsInNoEffect() {
    cache.put(sample.getId(), sample);

    long initialTimeMillis = System.currentTimeMillis() - 1;
    supervisedService.start(sample.getId(), new Timestamp(initialTimeMillis));
    assertEquals(initialTimeMillis, stateTagService.getSupervisionEvent(sample.getStateTagId()).getEventTime().getTime());

    long timeOfRunningNotStartupStatus = initialTimeMillis + 1;
    supervisedService.resume(sample.getId(), new Timestamp(timeOfRunningNotStartupStatus), "");
    SupervisionEvent supervisionEvent = stateTagService.getSupervisionEvent(sample.getStateTagId());
    assertEquals(timeOfRunningNotStartupStatus, supervisionEvent.getEventTime().getTime());
    assertEquals(RUNNING, supervisionEvent.getStatus());

    // This should have no change
    supervisedService.resume(sample.getId(), new Timestamp(initialTimeMillis + 2), "");
    supervisionEvent = stateTagService.getSupervisionEvent(sample.getStateTagId());
    assertEquals(timeOfRunningNotStartupStatus, supervisionEvent.getEventTime().getTime());
    assertEquals(RUNNING, supervisionEvent.getStatus());
  }

  private void verifySupervisionEvent(SupervisionStatus expectedStatus) {
    SupervisionEvent event = stateTagService.getSupervisionEvent(sample.getStateTagId());

    assertEquals(sample.getStateTagId().longValue(), event.getEntityId());
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

//    assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    Thread.sleep(100);

    // Cache object has achieved expected status
    verifySupervisionEvent(expected);
  }
}
