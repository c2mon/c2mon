package cern.c2mon.cache;

import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public abstract class SupervisedServiceTest<T extends Supervised> extends SupervisedServiceListenerTest<T> {

  @Test(expected = CacheElementNotFoundException.class)
  public void getSupervisionStatusThrowsIfNonexistent() {
    supervisedService.getSupervisionStatus(sample.getId());
  }

  @Test
  public void getSupervisionStatus() {
    cache.put(sample.getId(), sample);

    verifySupervisionEvent(sample, SupervisionConstants.SupervisionStatus.DOWN);
  }

  private void verifySupervisionEvent(Supervised supervised, SupervisionConstants.SupervisionStatus expectedStatus) {
    SupervisionEvent event = supervisedService.getSupervisionStatus(supervised.getId());

    assertEquals(supervised.getId(), event.getEntityId());
    assertEquals(event.getEntity(), supervised.getSupervisionEntity());
    assertEquals(expectedStatus, event.getStatus());

    // Repeating the attempt yields an equal result
    assertEquals(event, supervisedService.getSupervisionStatus(supervised.getId()));
  }

  @Test
  public void start() {
    cacheSupervision(() -> supervisedService.start(sample.getId(), Timestamp.from(Instant.now())),
      SupervisionConstants.SupervisionStatus.STARTUP);
  }

  @Test
  public void stop() {
    cacheSupervision(() -> supervisedService.stop(sample.getId(), Timestamp.from(Instant.now())),
      SupervisionConstants.SupervisionStatus.DOWN);
  }

  @Test
  public void suspend() {
    cacheSupervision(() -> supervisedService.suspend(sample.getId(), Timestamp.from(Instant.now()), ""),
      SupervisionConstants.SupervisionStatus.DOWN);
  }

  @Test
  public void resume() {
    cacheSupervision(() -> supervisedService.resume(sample.getId(), Timestamp.from(Instant.now()), ""),
      SupervisionConstants.SupervisionStatus.RUNNING);
  }

  private void cacheSupervision(Supplier<T> cacheAction, SupervisionConstants.SupervisionStatus expected) {
    cache.put(sample.getId(), sample);

    T cacheObj = cacheAction.get();

    // Cache object has achieved expected status
    verifySupervisionEvent(cacheObj, expected);
    // Source object has not been affected
    assertEquals(SupervisionConstants.SupervisionStatus.DOWN, sample.getSupervisionStatus());
  }

  @Test
  public void isRunning() {
    cache.put(sample.getId(), sample);
    // Default
    assertFalse(supervisedService.isRunning(sample.getId()));

    sample.setSupervision(SupervisionConstants.SupervisionStatus.STARTUP, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertTrue(supervisedService.isRunning(sample.getId()));

    sample.setSupervision(SupervisionConstants.SupervisionStatus.RUNNING_LOCAL, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertTrue(supervisedService.isRunning(sample.getId()));

    sample.setSupervision(SupervisionConstants.SupervisionStatus.RUNNING, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertTrue(supervisedService.isRunning(sample.getId()));

    sample.setSupervision(SupervisionConstants.SupervisionStatus.STOPPED, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertFalse(supervisedService.isRunning(sample.getId()));

    sample.setSupervision(SupervisionConstants.SupervisionStatus.DOWN, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertFalse(supervisedService.isRunning(sample.getId()));

    sample.setSupervision(SupervisionConstants.SupervisionStatus.UNCERTAIN, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertFalse(supervisedService.isRunning(sample.getId()));
  }

  @Test
  public void isUncertain() {
    sample.setSupervision(SupervisionConstants.SupervisionStatus.UNCERTAIN, "", new Timestamp(0));

    cache.put(sample.getId(), sample);

    verifySupervisionEvent(sample, SupervisionConstants.SupervisionStatus.UNCERTAIN);
    assertTrue(supervisedService.isUncertain(sample.getId()));
  }
}
