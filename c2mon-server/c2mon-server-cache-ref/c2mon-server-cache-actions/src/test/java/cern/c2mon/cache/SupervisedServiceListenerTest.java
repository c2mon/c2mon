package cern.c2mon.cache;

import cern.c2mon.cache.actions.supervision.SupervisedCacheService;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.CacheEvent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class SupervisedServiceListenerTest<T extends Supervised> extends MultiThreadCacheListenerTest<T> {

  protected SupervisedCacheService<T> supervisedService;

  protected abstract SupervisedCacheService<T> getSupervisedService();

  // TODO Test with other kinds of listeners too?

  @Before
  @Override
  public void resetResults() {
    super.resetResults();
    supervisedService = getSupervisedService();
  }

  @Test
  public void refreshAndNotifyCurrentSupervisionStatus() {
    // Generates one supervision update event, we don't listen yet
    cache.put(1L, sample);

    cache.registerListener(paramListener, CacheEvent.SUPERVISION_UPDATE);

    // Should generate exactly one event
    supervisedService.refresh(sample.getId());

    paramListener.close();

    assertEquals(1, eventCounter.get());
  }

  @Test
  public void supervisionChangeNotification() {
    registerListenerAndPut(paramListener, CacheEvent.SUPERVISION_CHANGE);

    assertEquals(1, eventCounter.get());
  }

  @Test
  public void supervisionChangeNotFiredWhenSame() {
    cache.put(sample.getId(), sample);
    // A more detailed test of when supervision events are fired based on status is in AbstractSupervisedCacheObjectTest
    registerListenerAndPut(paramListener, CacheEvent.SUPERVISION_CHANGE);
    assertEquals(0, eventCounter.get());
  }

  @Test
  public void supervisionChangePassesCloneObject() {
    registerListenerAndPut(mutatingListener, CacheEvent.SUPERVISION_CHANGE);

    assertEquals(1, eventCounter.get());
    assertEquals(sample, cache.get(sample.getId()));
  }

  @Test
  public void supervisionUpdateNotification() {
    registerListenerAndPut(paramListener, CacheEvent.SUPERVISION_UPDATE);
    assertEquals(1, eventCounter.get());
  }

  @Test
  public void supervisionUpdatePassesCloneObject() {
    registerListenerAndPut(mutatingListener, CacheEvent.SUPERVISION_UPDATE);

    assertEquals(1, eventCounter.get());
    assertEquals(sample, cache.get(sample.getId()));
  }
}
