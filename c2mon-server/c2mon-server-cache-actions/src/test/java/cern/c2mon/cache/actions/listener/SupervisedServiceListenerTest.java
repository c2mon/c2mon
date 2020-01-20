package cern.c2mon.cache.actions.listener;

import cern.c2mon.cache.AbstractCacheTest;
import cern.c2mon.cache.actions.supervision.SupervisedCacheService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.CacheListenerManagerImpl;
import cern.c2mon.server.common.equipment.AbstractSupervisedCacheObject;
import cern.c2mon.server.common.supervision.Supervised;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.Before;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class SupervisedServiceListenerTest<T extends Supervised, T_IMPL extends AbstractSupervisedCacheObject>
  extends AbstractCacheTest<T, T_IMPL> {

  private final AtomicInteger eventCounter = new AtomicInteger(0);

  protected SupervisedCacheService<T> supervisedService;

  @Getter(AccessLevel.PROTECTED)
  protected C2monCache<T> cache;

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
}
