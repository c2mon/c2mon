package cern.c2mon.cache.api.listener;

import java.util.concurrent.CountDownLatch;

import cern.c2mon.shared.common.Cacheable;

import static org.easymock.EasyMock.*;

/**
 * @author Szymon Halastra
 */
public class CacheListenerBaseTest {

  protected void testNotification(CacheListener c2monCacheListener, CacheListener mockTimCacheListener) throws InterruptedException {
    final Cacheable mockCacheable = createMock(Cacheable.class);

    // expect the C2monCacheListener to be notified of the update by the ThreadHandler in the
    // asynchronous listener
    CountDownLatch latch = new CountDownLatch(1);

    mockTimCacheListener.notifyElementUpdated(mockCacheable);
    expectLastCall().andAnswer(() -> {
      latch.countDown();
      return null;
    });

    //replay the scenario, notifying of the update
    replay(mockCacheable);
    replay(mockTimCacheListener);
    c2monCacheListener.notifyElementUpdated(mockCacheable);
    latch.await();
    verify(mockTimCacheListener);
    verify(mockCacheable);
  }
}
