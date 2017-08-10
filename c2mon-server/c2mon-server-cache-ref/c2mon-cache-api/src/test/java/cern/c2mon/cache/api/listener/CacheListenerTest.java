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

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.cache.api.listener.impl.CacheListener;
import cern.c2mon.shared.common.Cacheable;

import static org.easymock.EasyMock.*;

/**
 * Unit test testing the CacheListener class functionality (asynchronous listener).
 * @author mbrightw
 *
 */
public class CacheListenerTest {

  /**
   * Class to test.
   */
  private CacheListener cacheListener;
  
  /**
   * Mocked listener waiting to be notified of updates.
   */
  private C2monCacheListener mockTimCacheListener;
  
  /**
   * Before each test method.
   */
  @Before
  public void setUp() {
    mockTimCacheListener = createMock(C2monCacheListener.class);
    cacheListener = new CacheListener(mockTimCacheListener);
    cacheListener.start();
  }
  
  /**
   * Tests the mock listener is notified of an update.
   * @throws CloneNotSupportedException should not normally be thrown
   * @throws InterruptedException if exception while waiting (need to wait for second thread to process update)
   */
  @Test
  public void testNotifyElementUpdated() throws CloneNotSupportedException, InterruptedException {
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
    cacheListener.notifyElementUpdated(mockCacheable);
    latch.await();
    verify(mockTimCacheListener);
    verify(mockCacheable);
  }
  
}
