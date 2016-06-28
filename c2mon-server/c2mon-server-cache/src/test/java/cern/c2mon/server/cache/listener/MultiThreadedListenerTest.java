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
package cern.c2mon.server.cache.listener;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.shared.common.Cacheable;

/**
 * Unit test of the MultiThreadedCacheListener class
 * (so no Spring context).
 * 
 * @author Mark Brightwell
 *
 */
public class MultiThreadedListenerTest {

  /**
   * Class to test.
   */
  private MultiThreadedCacheListener multiThreadedListener;
  
  /**
   * Mock listener.
   */
  private C2monCacheListener mockTimCacheListener;

  @Before
  public void setUp() {
    mockTimCacheListener = createMock(C2monCacheListener.class);
    multiThreadedListener = new MultiThreadedCacheListener(mockTimCacheListener, 2, 2);
  }
  
  @Test
  public void testNotification() throws CloneNotSupportedException, InterruptedException {
    final Cacheable mockCacheable = createMock(Cacheable.class);
      
    //expect clone to be called twice: just return the same object for the testing -- cloning no longer done by listener implementation
//    expect(mockCacheable.clone()).andAnswer(new IAnswer<Cacheable>() {
//      public Cacheable answer() throws Throwable { 
//        return mockCacheable;
//      }
//    });
    
    //expect the C2monCacheListener to be notified of the update by the ThreadHandler in the
    //  asynchronous listener
    mockTimCacheListener.notifyElementUpdated(mockCacheable);
    
    //replay the scenario, notifying of the update
    replay(mockCacheable);        
    replay(mockTimCacheListener);
    multiThreadedListener.notifyElementUpdated(mockCacheable);
    Thread.sleep(1000); //otherwise verify before processed by other thread
    verify(mockTimCacheListener);
    verify(mockCacheable);
  }
  
  @Test
  public void testTwoNotifications() throws CloneNotSupportedException, InterruptedException {
    final Cacheable mockCacheable = createMock(Cacheable.class);
    final Cacheable mockCacheable2 = createMock(Cacheable.class); //remember must not notify with the same object twice    
    //expect clone to be called twice: just return the same object for the testing - cloning no longer done for each listener
//    expect(mockCacheable.clone()).andAnswer(new IAnswer<Cacheable>() {
//      public Cacheable answer() throws Throwable { 
//        return mockCacheable;
//      }
//    });
//    expect(mockCacheable.clone()).andAnswer(new IAnswer<Cacheable>() {
//      public Cacheable answer() throws Throwable { 
//        return mockCacheable;
//      }
//    });

    mockTimCacheListener.notifyElementUpdated(mockCacheable);
    mockTimCacheListener.notifyElementUpdated(mockCacheable2);
    
    //replay the scenario, notifying of the update
    replay(mockCacheable);  
    replay(mockCacheable2);
    replay(mockTimCacheListener);
    multiThreadedListener.notifyElementUpdated(mockCacheable);    
    multiThreadedListener.notifyElementUpdated(mockCacheable2);
    Thread.sleep(20000); //otherwise verify before processed by other thread
    verify(mockTimCacheListener);
    verify(mockCacheable);
    verify(mockCacheable2);   
  }
  
  /**
   * Is now allowed but logs a warning
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
