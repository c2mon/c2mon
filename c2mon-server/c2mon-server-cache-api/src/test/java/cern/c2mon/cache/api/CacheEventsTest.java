package cern.c2mon.cache.api;

import cern.c2mon.cache.api.impl.SimpleC2monCache;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.shared.common.CacheEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static cern.c2mon.shared.common.CacheEvent.*;
import static org.junit.Assert.assertEquals;

public class CacheEventsTest {
  private static final AtomicInteger eventCounter = new AtomicInteger(0);

  private final Alarm sample = new AlarmCacheObject(1L);
  private final CacheListener<Alarm> listenerAction = eq -> eventCounter.incrementAndGet();
  private C2monCache<Alarm> cache = new SimpleC2monCache<>("alarm");

  @Before
  public void reset() {
    cache.clear();
    eventCounter.set(0);
  }

  @Test
  public void putSendsAcceptedEvent() {
    doAndListenToEvents(() -> cache.put(sample.getId(), sample), UPDATE_ACCEPTED);

    assertEquals(1, eventCounter.get());
  }

  @Test
  public void firstPutSendsInsertedEvent() {
    doAndListenToEvents(() -> cache.put(sample.getId(), sample), INSERTED);

    assertEquals(1, eventCounter.get());
  }

  @Test
  public void updatePutSendsOnlyAcceptedEvent() {
    cache.put(sample.getId(), sample);
    doAndListenToEvents(() -> cache.put(sample.getId(), sample), CacheEvent.values());

    assertEquals(1, eventCounter.get());
  }

  @Test
  public void putQuietDoesNotSendEvents() {
    doAndListenToEvents(() -> cache.putQuiet(sample.getId(), sample), CacheEvent.values());

    assertEquals(0, eventCounter.get());
  }

  @Test
  public void putEqualsSendsRejectedEvent() {
    doAndListenToEvents(() -> {
      cache.put(sample.getId(), sample);
      cache.put(sample.getId(), sample);
    }, UPDATE_REJECTED);

    assertEquals(1, eventCounter.get());
  }

  private void doAndListenToEvents(Runnable runnable, CacheEvent... cacheEvents) {
    cache.getCacheListenerManager().registerListener(listenerAction, cacheEvents[0], cacheEvents);

    runnable.run();

    cache.getCacheListenerManager().close();
  }
}
