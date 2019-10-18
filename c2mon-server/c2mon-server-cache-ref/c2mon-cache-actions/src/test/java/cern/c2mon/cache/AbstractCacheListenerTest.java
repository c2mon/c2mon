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
  protected final CacheListener<V> mutatingListenerAction = cacheable -> {
    eventCounter.incrementAndGet();
    mutateObject(cacheable);
  };

  protected AbstractCacheListener<V> paramListener;
  protected AbstractCacheListener<V> mutatingListener;
  protected V sample;

  protected abstract AbstractCacheListener<V> generateListener();

  protected abstract AbstractCacheListener<V> generateMutatingListener();

  protected abstract void mutateObject(V cacheable);

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
    registerListenerAndPut(paramListener, CacheEvent.UPDATE_ACCEPTED);
    assertEquals(1, eventCounter.get());
  }

  @Test
  public void updatePassesCloneObject() {
    registerListenerAndPut(mutatingListener, CacheEvent.UPDATE_ACCEPTED);
    assertEquals("Sample object should not have been mutated! ", sample, cache.get(sample.getId()));
  }

  @Test
  public void putQuietDoesNotSendNotification() {
    cache.registerListener(paramListener, CacheEvent.values());

    cache.putQuiet(1L, sample);

    assertEquals(1, cache.getKeys().size());

    paramListener.close();
    assertEquals("putQuiet should not create any events", 0, eventCounter.get());

  }

  protected void registerListenerAndPut(AbstractCacheListener<V> listener, CacheEvent... events) {
    cache.registerListener(listener, events);

    cache.put(1L, sample);

    assertEquals(1, cache.getKeys().size());

    listener.close();
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
