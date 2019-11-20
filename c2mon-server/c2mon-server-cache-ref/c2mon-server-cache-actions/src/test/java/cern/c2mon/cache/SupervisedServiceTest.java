package cern.c2mon.cache;

import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.function.Supplier;

import static cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import static cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus.*;
import static org.junit.Assert.*;

public abstract class SupervisedServiceTest<T extends Supervised> extends SupervisedServiceListenerTest<T> {
  private static final int STATUSES_SIZE = values().length;

  @Test(expected = CacheElementNotFoundException.class)
  public void getSupervisionStatusThrowsIfNonexistent() {
    supervisedService.getSupervisionEvent(sample.getId());
  }

  @Test
  public void getSupervisionStatus() {
    cache.put(sample.getId(), sample);

    verifySupervisionEvent(sample, DOWN);
  }

  private void verifySupervisionEvent(Supervised supervised, SupervisionStatus expectedStatus) {
    SupervisionEvent event = supervisedService.getSupervisionEvent(supervised.getId());

    assertEquals(supervised.getId(), event.getEntityId());
    assertEquals(event.getEntity(), supervised.getSupervisionEntity());
    assertEquals(expectedStatus, event.getStatus());

    // Repeating the attempt yields an equal result
    assertEquals(event, supervisedService.getSupervisionEvent(supervised.getId()));
  }

  @Test
  public void start() {
    cacheSupervision(() -> supervisedService.start(sample.getId(), Timestamp.from(Instant.now())),
      STARTUP);
  }

  @Test
  public void stop() {
    cacheSupervision(() -> supervisedService.stop(sample.getId(), Timestamp.from(Instant.now())),
      DOWN);
  }

  @Test
  public void suspend() {
    cacheSupervision(() -> supervisedService.suspend(sample.getId(), Timestamp.from(Instant.now()), ""),
      DOWN);
  }

  @Test
  public void resume() {
    cacheSupervision(() -> supervisedService.resume(sample.getId(), Timestamp.from(Instant.now()), ""),
      RUNNING);
  }

  private void cacheSupervision(Supplier<T> cacheAction, SupervisionStatus expected) {
    cache.put(sample.getId(), sample);

    T cacheObj = cacheAction.get();

    // Cache object has achieved expected status
    verifySupervisionEvent(cacheObj, expected);
    // Source object has not been affected
    assertEquals(DOWN, sample.getSupervisionStatus());
  }

  @Test
  public void isRunning() {
    cache.put(sample.getId(), sample);
    // Default
    assertFalse(supervisedService.isRunning(sample.getId()));

    sample.setSupervision(STARTUP, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertTrue(supervisedService.isRunning(sample.getId()));

    sample.setSupervision(RUNNING_LOCAL, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertTrue(supervisedService.isRunning(sample.getId()));

    sample.setSupervision(RUNNING, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertTrue(supervisedService.isRunning(sample.getId()));

    sample.setSupervision(STOPPED, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertFalse(supervisedService.isRunning(sample.getId()));

    sample.setSupervision(DOWN, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertFalse(supervisedService.isRunning(sample.getId()));

    sample.setSupervision(UNCERTAIN, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertFalse(supervisedService.isRunning(sample.getId()));
  }

  @Test
  public void isUncertain() {
    sample.setSupervision(UNCERTAIN, "", new Timestamp(0));

    cache.put(sample.getId(), sample);

    verifySupervisionEvent(sample, UNCERTAIN);
    assertTrue(supervisedService.isUncertain(sample.getId()));
  }

  @Override
  protected void mutateObject(T supervised) {
    SupervisionStatus nextStatus = values()[(supervised.getSupervisionStatus().ordinal() + 1) % STATUSES_SIZE];
    supervised.setSupervision(nextStatus, "It's always 42", Timestamp.from(Instant.MIN));
  }
}
