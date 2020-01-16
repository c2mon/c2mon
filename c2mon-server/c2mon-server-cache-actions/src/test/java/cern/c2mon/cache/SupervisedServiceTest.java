package cern.c2mon.cache;

import cern.c2mon.cache.actions.listener.SupervisedServiceListenerTest;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.server.common.equipment.AbstractSupervisedCacheObject;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.function.Supplier;

import static cern.c2mon.shared.common.supervision.SupervisionStatus.*;
import static org.junit.Assert.assertEquals;

public abstract class SupervisedServiceTest<T extends Supervised, T_IMPL extends AbstractSupervisedCacheObject>
  extends SupervisedServiceListenerTest<T, T_IMPL> {

  @Inject
  SupervisionStateTagService stateTagService;

  @Test
  public void getSupervisionStatus() {
    cache.put(sample.getId(), sample);

    verifySupervisionEvent(sample, DOWN);
  }

  @Test
  public void start() {
    cacheSupervision(
      () -> supervisedService.start(sample.getId(), Timestamp.from(Instant.now())),
      STARTUP);
  }

  @Test
  public void stop() {
    cacheSupervision(
      () -> supervisedService.stop(sample.getId(), Timestamp.from(Instant.now())),
      DOWN);
  }

  @Test
  public void suspend() {
    cacheSupervision(
      () -> supervisedService.suspend(sample.getId(), Timestamp.from(Instant.now()), ""),
      DOWN);
  }

  @Test
  public void resume() {
    cacheSupervision(
      () -> supervisedService.resume(sample.getId(), Timestamp.from(Instant.now()), ""),
      RUNNING);
  }

  @Test
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

  private void verifySupervisionEvent(Supervised supervised, SupervisionStatus expectedStatus) {
    SupervisionEvent event = stateTagService.getSupervisionEvent(supervised.getStateTagId());

    assertEquals(supervised.getStateTagId().longValue(), event.getEntityId());
    assertEquals(event.getEntity(), supervised.getSupervisionEntity());
    assertEquals(expectedStatus, event.getStatus());

    // Repeating the attempt yields an equal result
    assertEquals(event, stateTagService.getSupervisionEvent(supervised.getStateTagId()));
  }

  private void cacheSupervision(Supplier<T> cacheAction, SupervisionStatus expected) {
    cache.put(sample.getId(), sample);

    T cacheObj = cacheAction.get();

    // Cache object has achieved expected status
    verifySupervisionEvent(cacheObj, expected);
    // Source object has not been affected
    assertEquals(DOWN, sample.getSupervisionStatus());
  }
}
