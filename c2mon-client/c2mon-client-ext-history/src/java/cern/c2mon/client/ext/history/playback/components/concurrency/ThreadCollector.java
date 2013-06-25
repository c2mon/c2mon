/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.ext.history.playback.components.concurrency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is for collecting lists of values from multiple threads. When
 * waiting for threads is over only ONE thread (the leader) returns with all the
 * collected values. The other threads are blocked until the leader calls
 * <code>release()</code>.<br/>
 * <br/>
 * Example of a method were you want to collect threads and their items:<br/>
 * <br/>
 * <code>
   private final ThreadCollector&lt;Long&gt; threadCollector = new ThreadCollector&lt;Long&gt;(800, 19000);<br/>
   <br/>
   private void initializeTagHistory(final Collection&lt;Long&gt; tags) {<br/>
<br/>    
    &nbsp;// Only the first thread that calls this<br/> 
    &nbsp;// method gets the list of all the tags.<br/>
    &nbsp;// For all the other threads it return null<br/>
    &nbsp;final Collection&lt;Long&gt; tagIds = threadCollector.collect(tags);<br/>
<br/>    
    &nbsp;if (tagIds == null) {<br/>
    &nbsp;&nbsp;return;<br/>
    &nbsp;}<br/>
<br/>
    &nbsp;// Main thread want to do some work<br/>
<br/>    
    &nbsp;// All the other threads are released, <br/>
    &nbsp;// ie. they return from the collect method<br/>
    &nbsp;threadCollector.release();<br/>
   }<br/>
   </code>
 * 
 * @author vdeila
 * 
 * @param <T>
 *          The type to collect
 */
public class ThreadCollector<T> {
  
  /**
   * When all the threads are ready to return, for how long is accepted to wait
   * for the last one(s)
   */
  private static final long WAITING_TIMEOUT = 5000;
  
  /**
   * No waiting limit, waits forever
   */
  public static final long NO_LIMIT = -1;
  
  /**
   * The collected items
   */
  private final List<T> collected;
  
  /**
   * Lock for <code>collected</code>
   */
  private final ReentrantLock collectedLock = new ReentrantLock();

  /**
   * The time it waits for a new one after just getting one
   */
  private long collectionWaitingTime;
  
  /**
   * The total time it can be extended (by the collectionWaitingTime)
   */
  private long collectionMaxTotalTime = 20000;
  
  /**
   * The time of when the last thread entered the waiting (by calling
   * collect(...))
   */
  private Long lastEntrantTime = null;
  
  /**
   * When the first entrant entered the waiting
   */
  private Long firstEntrantTime = null;
  
  /**
   * Lock for <code>lastEntrantTime</code> and <code>firstEntrantTime</code>
   */
  private ReentrantReadWriteLock entrantLock = new ReentrantReadWriteLock();
  
  /**
   * Guard lock to keep threads from return before their collection is returned
   */
  private ReentrantLock enteringGuard = new ReentrantLock();
  
  /**
   * Holds a count of how many threads have entered the waiting
   */
  private AtomicInteger numberOfThreadsEntered = new AtomicInteger();
  
  /**
   * Count of how many threads is waiting to be released
   */
  private AtomicInteger numberOfThreadsWaitingForRelease = new AtomicInteger();
  
  /**
   * Count of how many threads is waiting to enter the collecting. Newly entered
   * threads will wait until the previous batch is released.
   */
  private AtomicInteger numberOfThreadsWaitingToEnter = new AtomicInteger();
  
  /**
   * Whether or not to block all the collectors until returning the value to one
   * of them
   */
  private boolean blockCollecters = true;
  
  /**
   * CountDownLatches for each of the chunk of threads released together.
   */
  private final Map<Long, CountDownLatch> releaseLatches = new HashMap<Long, CountDownLatch>();
  
  /**
   * Lock for <code>releaseLatches</code>
   */
  private final ReentrantReadWriteLock releaseLatchesLock = new ReentrantReadWriteLock();
  
  /**
   * The CountDownLatch for the current queue of threads.
   */
  private CountDownLatch currentWaitingObject = null;
  
  /**
   * Lock for <code>currentWaitingObject</code>
   */
  private final ReentrantReadWriteLock currentWaitingObjectLock = new ReentrantReadWriteLock();
  
  /**
   * 
   * @param collectionWaitingTime
   *          The time it waits for a new one after just getting one
   * 
   * @param collectionMaxTotalTime
   *          The total time the waiting can be extended
   */
  public ThreadCollector(final long collectionWaitingTime, final long collectionMaxTotalTime) {
    this.collected = new ArrayList<T>();
    this.collectionWaitingTime = collectionWaitingTime;
    this.collectionMaxTotalTime = collectionMaxTotalTime;
  }
  
  /**
   * 
   * @param collectionWaitingTime
   *          The time it waits for a new one after just getting one
   */
  public ThreadCollector(final long collectionWaitingTime) {
    this.collected = new ArrayList<T>();
    this.collectionWaitingTime = collectionWaitingTime;
  }
  
  /**
   * 
   * @param collection
   *          The collection to add to the <code>collected</code> list
   * 
   * @return <code>true</code> if this thread should return with the collection
   *         at last
   */
  private boolean addCollectedItems(final Collection<T> collection) {
    boolean youWillReturnWithCollection = false;
    try {
      this.collectedLock.lock();
      this.entrantLock.writeLock().lock();
      
      // Updates the time of arrival
      if (this.firstEntrantTime == null) {
        // The current thread is now the leader / the thread which returns with the collected items
        
        this.firstEntrantTime = System.currentTimeMillis();
        
        // Adds this thread to the one who can later release all the other threads
        try {
          currentWaitingObjectLock.writeLock().lock();
          this.currentWaitingObject = new CountDownLatch(1);
        }
        finally {
          currentWaitingObjectLock.writeLock().unlock();
        }
        
        try {
          this.releaseLatchesLock.writeLock().lock();
          this.releaseLatches.put((Long) Thread.currentThread().getId(), this.currentWaitingObject);
        }
        finally {
          this.releaseLatchesLock.writeLock().unlock();
        }

        youWillReturnWithCollection = true;
      }
      this.lastEntrantTime = System.currentTimeMillis();
      
      // Adds it to the collection
      this.collected.addAll(collection);
    }
    finally {
      this.entrantLock.writeLock().unlock();
      this.collectedLock.unlock();
    }
    return youWillReturnWithCollection;
  }
  
  /**
   * Copies the list, and removes the elements from the <code>collected</code> list.
   * Also sets firstEntrantTime and lastEntrantTime to null
   * 
   * @return The collected items
   */
  private Collection<T> withdrawCollectedItems() {
    try {
      this.collectedLock.lock();
      this.entrantLock.writeLock().lock();
      
      this.firstEntrantTime = null;
      this.lastEntrantTime = null;
      
      final List<T> collectedItems = new ArrayList<T>(this.collected);
      this.collected.clear();
      
      return collectedItems;
    }
    finally {
      this.entrantLock.writeLock().unlock();
      this.collectedLock.unlock();
    }
  }
  
  /**
   * 
   * @return The time to wait. Negative if the collector should return
   */
  private long calculateWaitTime() {
    try {
      this.entrantLock.writeLock().lock();
      Long waitToTime = null;
      
      // Calculates the ending time according to the collectionWaitingTime
      if (this.collectionWaitingTime != NO_LIMIT) {
        waitToTime = this.lastEntrantTime + this.collectionWaitingTime;
      }
      
      // Calculates the ending time according to the collectionMaxTotalTime
      if (this.collectionMaxTotalTime != NO_LIMIT) {
        long waitingToTimeTotalTime = this.firstEntrantTime + this.collectionMaxTotalTime;
        if (waitToTime == null || waitToTime > waitingToTimeTotalTime) {
          waitToTime = waitingToTimeTotalTime;
        }
      }
      
      if (waitToTime == null) {
        return 0;
      }
      
      return waitToTime - System.currentTimeMillis();
    }
    finally {
      this.entrantLock.writeLock().unlock();
    }
  }
  
  /**
   * 
   * 
   * @param items
   *          The items to collect
   * @return The collected items. Only one thread gets the collected items, all
   *         the others gets <code>null</code>
   */
  public Collection<T> collect(final Collection<T> items) {
    try {
      this.numberOfThreadsWaitingToEnter.incrementAndGet();
      try {
        this.enteringGuard.lock();
        this.numberOfThreadsEntered.incrementAndGet();
      }
      finally {
        this.enteringGuard.unlock();
      }
    }
    finally {
      this.numberOfThreadsWaitingToEnter.decrementAndGet();
    }
    
    // Collects the items
    boolean iWillReturnWithCollection = addCollectedItems(items);
    
    if (blockCollecters || iWillReturnWithCollection) {
      // Waits for the other collectors
      while (true) {
        final long waitTime = calculateWaitTime();
        if (waitTime <= 0) {
          break;
        }
        try {
          Thread.sleep(waitTime);
        }
        catch (InterruptedException e) { }
      }
    }
    
    if (iWillReturnWithCollection) {
      // The first thread waits and returns with the collection
      try {
        // It is not any more time for more threads to collect the items
        this.enteringGuard.lock();
        
        this.numberOfThreadsEntered.decrementAndGet();
        
        // Waits for the entered threads to collect their items and to be ready to return
        waitForReturn();
        
        return this.withdrawCollectedItems();
      }
      finally {
        this.enteringGuard.unlock();
      }
    }
    else {
      final CountDownLatch waitingObject = getCurrentWaitingObject();
      
      try {
        this.numberOfThreadsWaitingForRelease.incrementAndGet();
        this.numberOfThreadsEntered.decrementAndGet();
        
        if (this.blockCollecters) {
          // Waits for the leader thread to release this thread
          try {
            waitingObject.await();
          }
          catch (InterruptedException e) { }
        }
      }
      finally {
        this.numberOfThreadsWaitingForRelease.decrementAndGet();
      }
      
      // All the threads returns null, except the leader thread
      return null;
    }
  }
  
  /**
   * 
   * @return How many threads that is at this moment waiting to be released.
   *         This does NOT include threads already entered for the next batch!
   */
  public int getNumberOfThreadsWaitingForRelease() {
    return this.numberOfThreadsWaitingForRelease.get();
  }
  
  /**
   * 
   * @return Count of how many threads is waiting to enter the collecting. Newly
   *         entered threads will wait until the previous batch is released.
   */
  public int getNumberOfThreadsWaitingToEnter() {
    return this.numberOfThreadsWaitingToEnter.get();
  }
  
  /**
   * 
   * @return The CountDownLatch for the current queue of threads.
   */
  private CountDownLatch getCurrentWaitingObject() {
    try {
      this.currentWaitingObjectLock.readLock().lock();
      return this.currentWaitingObject;
    }
    finally {
      this.currentWaitingObjectLock.readLock().unlock();
    }
    
  }
  
  /**
   * Waits for the entered threads to collect their items and to be ready to return
   */
  private void waitForReturn() {
    final long waitingMaxToTime = System.currentTimeMillis() + WAITING_TIMEOUT;
    
    while (this.numberOfThreadsEntered.get() > 0 && System.currentTimeMillis() < waitingMaxToTime) {
      try {
        Thread.sleep(20);
      }
      catch (InterruptedException e) { }
    }
  }
  
  /**
   * Only the leader thread (the thread which got the return value) can call
   * this method. This makes all the other threads return from the
   * <code>collect(...)</code> method..
   * 
   * @return <code>true</code> if successfully released the threads..
   */
  public boolean release() {
    try {
      this.releaseLatchesLock.writeLock().lock();
      final CountDownLatch countDownLatch = this.releaseLatches.remove((Long) Thread.currentThread().getId());
      if (countDownLatch != null) {
        countDownLatch.countDown();
        return true;
      }
      else {
        return false;
      }
    }
    finally {
      this.releaseLatchesLock.writeLock().unlock(); 
    }
  }

  /**
   * @return the collectionWaitingTime
   */
  public long getCollectionWaitingTime() {
    return collectionWaitingTime;
  }

  /**
   * @param collectionWaitingTime the collectionWaitingTime to set
   */
  public void setCollectionWaitingTime(final long collectionWaitingTime) {
    this.collectionWaitingTime = collectionWaitingTime;
  }

  /**
   * @return the collectionMaxTotalTime
   */
  public long getCollectionMaxTotalTime() {
    return collectionMaxTotalTime;
  }

  /**
   * @param collectionMaxTotalTime the collectionMaxTotalTime to set
   */
  public void setCollectionMaxTotalTime(final long collectionMaxTotalTime) {
    this.collectionMaxTotalTime = collectionMaxTotalTime;
  }

  /**
   * @return the blockCollecters
   */
  public boolean isBlockCollecters() {
    return blockCollecters;
  }

  /**
   * @param blockCollecters the blockCollecters to set
   */
  public void setBlockCollecters(boolean blockCollecters) {
    this.blockCollecters = blockCollecters;
  }
  
  
}
