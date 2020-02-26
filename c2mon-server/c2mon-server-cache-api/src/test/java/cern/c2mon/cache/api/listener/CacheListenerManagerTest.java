package cern.c2mon.cache.api.listener;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.shared.common.CacheEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CacheListenerManagerTest {
  private static final AtomicInteger eventCounter = new AtomicInteger(0);

  private final Alarm sample = new AlarmCacheObject(1L);
  private final CacheListener<Alarm> listenerAction = eq -> eventCounter.incrementAndGet();
  private final CacheListener<Alarm> mutatingListenerAction = alarm -> {
    eventCounter.incrementAndGet();
    ((AlarmCacheObject) alarm).setInfo("ABC");
  };

  private final CacheListenerManager<Alarm> cacheListenerManager = new CacheListenerManagerImpl<>();

  @Before
  public void reset() {
    eventCounter.set(0);
  }

  @Test
  public void eventNotifies() {
    registerListenerAndDo(listenerAction, () -> {
      cacheListenerManager.notifyListenersOf(CacheEvent.UPDATE_ACCEPTED, sample);
    });

    assertEquals(1, eventCounter.get());
  }

  @Test
  public void eventPassesCloneObject() {
    registerListenerAndDo(mutatingListenerAction, () -> {
      cacheListenerManager.notifyListenersOf(CacheEvent.UPDATE_ACCEPTED, sample);
    });

    assertEquals(1, eventCounter.get());
    assertNotEquals("ABC", sample.getInfo());
  }

  @Test
  public void registeredBufferListener() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);

    BatchConsumer<Alarm> alarmBufferedCacheListener = alarms ->
      alarms.forEach( __ -> latch.countDown() );

    cacheListenerManager.registerBufferedListener(alarmBufferedCacheListener, CacheEvent.UPDATE_ACCEPTED);

    cacheListenerManager.notifyListenersOf(CacheEvent.UPDATE_ACCEPTED, sample);
    latch.await(1100, TimeUnit.MILLISECONDS);
    cacheListenerManager.close();
  }

  @Test
  public void multipleEventsDontGetLost() {
    int repetitions = 100;

    registerListenerAndDo(listenerAction, () -> {

      for (int i = 0; i < repetitions; i++) {
        cacheListenerManager.notifyListenersOf(CacheEvent.UPDATE_ACCEPTED, sample);
      }
    });

    assertEquals(repetitions, eventCounter.get());
  }

  /**
   * Use {@link CacheEvent#UPDATE_ACCEPTED} for your notifications
   */
  private void registerListenerAndDo(CacheListener<Alarm> listener, Runnable runnable) {
    cacheListenerManager.registerListener(listener, CacheEvent.UPDATE_ACCEPTED);

    runnable.run();

    cacheListenerManager.close();
  }


}
