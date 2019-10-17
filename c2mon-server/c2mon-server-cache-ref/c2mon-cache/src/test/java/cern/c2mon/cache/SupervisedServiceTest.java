package cern.c2mon.cache;

import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.supervision.SupervisedService;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public abstract class SupervisedServiceTest<T extends Supervised> extends MultiThreadCacheListenerTest<T> {
  //  protected final AtomicInteger eventCounter = new AtomicInteger(0);
//  protected CacheListener<V> listenerAction = eq -> eventCounter.incrementAndGet();
  protected SupervisedService<T> supervisedService;

  protected abstract SupervisedService<T> getSupervisedService();

  // TODO Test with other kinds of listeners too?

  @Before
  @Override
  public void resetResults() {
    super.resetResults();
    supervisedService = getSupervisedService();
  }

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


  // Listeners

  @Test
  public void refreshAndNotifyCurrentSupervisionStatus() {

  }

  @Test
  public void supervisionChangeNotification() {
    cache.registerListener(paramListener, CacheEvent.SUPERVISION_CHANGE);

    cache.put(1L, sample);

    paramListener.close();

    assertEquals(1, eventCounter.get());
  }

  @Test
  public void supervisionChangePassesCloneObject() {

  }

  @Test
  public void supervisionUpdateNotification() {
    cache.registerListener(paramListener, CacheEvent.SUPERVISION_UPDATE);

    cache.put(1L, sample);

    paramListener.close();

    assertEquals(1, eventCounter.get());
  }

  @Test
  public void supervisionUpdatePassesCloneObject() {

  }
}
