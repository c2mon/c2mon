/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.cache.api.listener;

import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.cache.api.listener.impl.MultiThreadedCacheListener;
import cern.c2mon.shared.common.Cacheable;

import static org.easymock.EasyMock.*;

/**
 * Unit test of the MultiThreadedCacheListener class
 * (so no Spring context).
 *
 * @author Mark Brightwell
 */
public class MultiThreadedListenerTest extends CacheListenerBaseTest {

  /**
   * Class to test.
   */
  private MultiThreadedCacheListener multiThreadedListener;

  /**
   * Mock listener.
   */
  private CacheListener cacheListener;

  @Before
  public void setUp() {
    cacheListener = createMock(CacheListener.class);
    multiThreadedListener = new MultiThreadedCacheListener(cacheListener, 2, 2);
  }

  @Test
  public void testNotification() throws CloneNotSupportedException, InterruptedException {
    testNotification(multiThreadedListener, cacheListener);
  }

  @Test
  public void testTwoNotifications() throws CloneNotSupportedException, InterruptedException {
    final Cacheable mockCacheable = createMock(Cacheable.class);
    final Cacheable mockCacheable2 = createMock(Cacheable.class); //remember must not notify with the same object twice    
    CountDownLatch latch = new CountDownLatch(2);

    cacheListener.notifyElementUpdated(mockCacheable);
    expectLastCall().andAnswer(() -> {
      latch.countDown();
      return null;
    });
    cacheListener.notifyElementUpdated(mockCacheable2);
    expectLastCall().andAnswer(() -> {
      latch.countDown();
      return null;
    });

    //replay the scenario, notifying of the update
    replay(mockCacheable);
    replay(mockCacheable2);
    replay(cacheListener);
    multiThreadedListener.notifyElementUpdated(mockCacheable);
    multiThreadedListener.notifyElementUpdated(mockCacheable2);
    latch.await();
    verify(cacheListener);
    verify(mockCacheable);
    verify(mockCacheable2);
  }

  /**
   * Is now allowed but logs a warning
   *
   * @throws InterruptedException
   */
  @Test //(expected=CacheException.class)
  public void testShutdown() throws InterruptedException {
    multiThreadedListener.stop();
    final Cacheable mockCacheable = createMock(Cacheable.class);
    multiThreadedListener.notifyElementUpdated(mockCacheable);
  }

  @After
  public void shutdown() {
    multiThreadedListener.stop();
  }

}
