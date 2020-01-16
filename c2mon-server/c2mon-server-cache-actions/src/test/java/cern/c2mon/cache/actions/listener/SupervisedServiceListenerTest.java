package cern.c2mon.cache.actions.listener;

import cern.c2mon.cache.AbstractCacheTest;
import cern.c2mon.cache.actions.supervision.SupervisedCacheService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.cache.api.listener.CacheListenerManagerImpl;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.CacheEvent;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public abstract class SupervisedServiceListenerTest<T extends Supervised> extends AbstractCacheTest<T> {

  private final AtomicInteger eventCounter = new AtomicInteger(0);
  private final CacheListener<T> paramListener = eq -> eventCounter.incrementAndGet();

  protected SupervisedCacheService<T> supervisedService;

  @Getter(AccessLevel.PROTECTED)
  protected C2monCache<T> cache;

  @Getter(AccessLevel.PROTECTED)
  protected T sample;

  protected abstract SupervisedCacheService<T> getSupervisedService();

  protected abstract C2monCache<T> initCache();

  // TODO (Alex) Test with other kinds of listeners too?

  @Before
  public void init() {
    cache = initCache();
    cache.setCacheListenerManager(new CacheListenerManagerImpl<>());
    eventCounter.set(0);
    supervisedService = getSupervisedService();
    sample = getSample();
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
  public void supervisionUpdateNotification() {
    registerListenerAndPut(paramListener, CacheEvent.SUPERVISION_UPDATE);
    assertEquals(1, eventCounter.get());
  }

  private void registerListenerAndPut(CacheListener<T> cacheListener, CacheEvent... cacheEvents) {
    cache.getCacheListenerManager().registerListener(cacheListener, cacheEvents);
    cache.put(sample.getId(), sample);
    cache.getCacheListenerManager().close();
  }
}
