package cern.c2mon.cache.api.listener;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.impl.SimpleC2monCache;
import cern.c2mon.cache.api.listener.impl.AbstractCacheListener;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public abstract class AbstractCacheListenerTest {
  protected static final AtomicInteger eventCounter = new AtomicInteger(0);
  protected static CacheListener<Equipment> listenerAction = eq -> eventCounter.incrementAndGet();
  protected static CacheListener<Equipment> mutatingListenerAction = eq -> {
    eventCounter.incrementAndGet();
    eq.getSubEquipmentIds().add(1L);
  };

  private AbstractCacheListener<Equipment> paramListener = null;
  private AbstractCacheListener<Equipment> mutatingListener = null;
  private C2monCache<Equipment> cache = new SimpleC2monCache<>("sample");
  private Equipment sample = new EquipmentCacheObject(1L, "Test-Eq", "Object", 100L);
  private Equipment sample2 = new EquipmentCacheObject(2L, "Test-Eq2", "Object", 101L);

  abstract AbstractCacheListener<Equipment> generateListener();

  abstract AbstractCacheListener<Equipment> generateMutatingListener();

  @Before
  public void resetResults() {
    eventCounter.set(0);
    cache.clear();
    if (paramListener != null)
      cache.deregisterListener(paramListener);
    if (mutatingListener != null)
      cache.deregisterListener(mutatingListener);
    paramListener = generateListener();
    mutatingListener = generateMutatingListener();
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
    assertEquals("Sample object should not have been mutated! ",0, sample.getSubEquipmentIds().size());
    assertEquals("Cache object should not have been mutated",0, cache.get(1L).getSubEquipmentIds().size());
  }

  @Test
  public void putQuietDoesNotSendNotification() {
    cache.registerListener(paramListener, CacheEvent.UPDATE_ACCEPTED);

    cache.putQuiet(1L, sample);

    assertEquals(1, cache.getKeys().size());

    paramListener.close();
  }

  @Test
  public void manyPutsDontGetLost() {
    cache.registerListener(paramListener, CacheEvent.UPDATE_ACCEPTED);

    int repetitions = 100_000;

    for (int i = 0; i < repetitions; i++)
      cache.put(1L, sample);

    assertEquals(1, cache.getKeys().size());

    paramListener.close();
    assertEquals(repetitions, eventCounter.get());
  }

  @Test
  public void supervisionChangeNotification() {

  }

  @Test
  public void supervisionChangePassesCloneObject() {

  }

  @Test
  public void supervisionUpdateNotification() {

  }

  @Test
  public void supervisionUpdatePassesCloneObject() {

  }

}
