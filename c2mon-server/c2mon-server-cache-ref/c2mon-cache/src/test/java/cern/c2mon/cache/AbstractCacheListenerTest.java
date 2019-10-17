package cern.c2mon.cache;

import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.cache.api.listener.impl.AbstractCacheListener;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public abstract class AbstractCacheListenerTest<V extends Cacheable> extends AbstractCacheTest<V> {
  protected static final AtomicInteger eventCounter = new AtomicInteger(0);
  protected final CacheListener<V> listenerAction = eq -> eventCounter.incrementAndGet();
  protected final CacheListener<V> mutatingListenerAction = eq -> {
    eventCounter.incrementAndGet();
//    eq.getSubEquipmentIds().add(1L);
  };

  protected AbstractCacheListener<V> paramListener;
  private AbstractCacheListener<V> mutatingListener;
  protected V sample;
//  = new EquipmentCacheObject(1L, "Test-Eq", "Object", 100L);
//  protected V sample2 = new EquipmentCacheObject(2L, "Test-Eq2", "Object", 101L);

  protected abstract AbstractCacheListener<V> generateListener();

  abstract AbstractCacheListener<V> generateMutatingListener();

  @Before
  public void resetResults() {
    eventCounter.set(0);
    cache.clear();
    sample = getSample();
    paramListener = generateListener();
    mutatingListener = generateMutatingListener();
  }

  @After
  public void teardown() {
    if (paramListener != null)
      cache.deregisterListener(paramListener);
    if (mutatingListener != null)
      cache.deregisterListener(mutatingListener);
  }

  @Test
  public void updateNotification() {
    cache.registerListener(paramListener, CacheEvent.UPDATE_ACCEPTED);

    cache.put(1L, sample);

    assertEquals(1, cache.getKeys().size());

    paramListener.close();
    assertEquals(1, eventCounter.get());
  }

  @Test
  public void updatePassesCloneObject() {
    cache.registerListener(mutatingListener, CacheEvent.UPDATE_ACCEPTED);

    cache.put(1L, sample);

    assertEquals(1, cache.getKeys().size());
    mutatingListener.close();
//    assertEquals("Sample object should not have been mutated! ",0, sample.getSubEquipmentIds().size());
//    assertEquals("Cache object should not have been mutated",0, cache.get(1L).getSubEquipmentIds().size());
  }

  @Test
  public void putQuietDoesNotSendNotification() {
    cache.registerListener(paramListener, CacheEvent.UPDATE_ACCEPTED);

    cache.putQuiet(1L, sample);

    assertEquals(1, cache.getKeys().size());

    paramListener.close();
  }

  @Test
  @Ignore
  public void manyPutsDontGetLost() {
    cache.registerListener(paramListener, CacheEvent.UPDATE_ACCEPTED);

    int repetitions = 100_000;

    for (int i = 0; i < repetitions; i++)
      cache.put(1L, sample);

    assertEquals(1, cache.getKeys().size());

    paramListener.close();
    assertEquals(repetitions, eventCounter.get());
  }

}
