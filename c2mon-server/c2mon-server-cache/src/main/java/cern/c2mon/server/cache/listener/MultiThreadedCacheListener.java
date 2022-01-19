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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.daq.lifecycle.Lifecycle;

/**
 * Cache listener implementation providing a pool of threads calling
 * the <code>notifyElementUpdated</code> on the {@link C2monCacheListener}
 * interface.
 * 
 * <p>This class is used internally in the server core to wrap a module
 * listener. The module should preferably register by using the
 * {@link CacheRegistrationService} bean.
 * 
 * <p>The number of threads used should be chosen according to the cache
 * the listener will be subscribed to and the expected operational load
 * of the system for that cache.
 * 
 * @author Mark Brightwell
 * @param <T> type of cache object expected by listener
 *
 */
@Slf4j
public class MultiThreadedCacheListener<T extends Cacheable> implements C2monCacheListener<T>, Lifecycle {
  
  /**
   * The number of milliseconds a thread waits between checking for shutdown requests.
   */
  private static final int THREAD_SHUTDOWN_CHECK_INTERVAL = 2000;
  
  /**
   * The wrapped listener.
   */
  private C2monCacheListener<T> c2monCacheListener;
  
  /**
   * Queue keeping the cache objects.
   */
  private LinkedBlockingQueue<ObjectAndMethod> taskQueue;  
  
  /**
   * Used for remembering which method to call (instead of reflection), in the map below.
   */
  private enum SupportedMethods { ON_UPDATE, STATUS_CONFIRMATION }
  
  /**
   * The pool of threads calling the C2monCacheListener (the threads are submitted
   * a fixed set of tasks that run until server shutdown).
   */
  private ThreadPoolExecutor executor;
    
  /**
   * Shutdown request made. On access lock using shutdownRequestLock.
   */
  private volatile boolean shutdownRequestMade = false;
  
  /**
   * The listener can only be started and stopped once.
   */
  private volatile boolean running = false;
  
  /**
   * Constructor.
   * @param timCacheListener the listener wrapped by this class
   *        (the module listener)
   * @param queueCapacity the capacity of the blocking queue 
   *        containing the cache objects to pass to the listeners
   * @param threadPoolSize the number of threads that the module 
   *        should be called on
   */
  public MultiThreadedCacheListener(final C2monCacheListener<T> timCacheListener, final int queueCapacity, final int threadPoolSize) {
    super();
    this.c2monCacheListener = timCacheListener;  
    taskQueue = new LinkedBlockingQueue<ObjectAndMethod>(queueCapacity);    
    executor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, Long.MAX_VALUE, TimeUnit.NANOSECONDS, new SynchronousQueue<Runnable>());
    for (int i = 0; i < threadPoolSize; i++) {
      executor.submit(new NotifyTask());     
    }    
  }
  
 
  @Override
  public void confirmStatus(T cacheable) {   
    try {
      if (!shutdownRequestMade) {              
        taskQueue.put(new ObjectAndMethod(cacheable, SupportedMethods.STATUS_CONFIRMATION));        
      } else {
        log.warn("Attempt at notifying of element update after shutdown started "
            + "- should not happen and indicates incorrect shutdown sequence!");       
      }            
    } catch (InterruptedException interEx) {
      log.error("InterruptedExcetion caught while waiting for MultiThreadedListener queue to free space: ", interEx);
    }
  }



  @Override
  public void notifyElementUpdated(T cacheable) {
    try {
      if (!shutdownRequestMade) {        
        taskQueue.put(new ObjectAndMethod(cacheable, SupportedMethods.ON_UPDATE));        
      } else {
        log.warn("Attempt at notifying of element update after shutdown started "
            + "- should not happen and indicates incorrect shutdown sequence!");
      }            
    } catch (InterruptedException interEx) {
      log.error("InterruptedExcetion caught while waiting for MultiThreadedListener queue to free space: ", interEx);
    }
  }

  /**
   * Is running until a shutdown request is made.
   */
  @Override
  public boolean isRunning() {
    return running;
  }

  /**
   * Can only be started once at server start-up.
   */
  @Override
  public void start() {
    running = true;
  }

  /**
   * Waits for all tasks to complete then shuts down the thread.
   * Should be called when the cache is closed on server shutdown.
   * Will have no effect if called a second time.
   */
  @Override
  public synchronized void stop() {
    long methodStartTime = System.nanoTime();
    if (running) {
      log.debug("Shutting down Multithreaded cache listener.");
      running = false;      
      shutdownRequestMade = true;        
      //wait for the queue to empty
      while (!taskQueue.isEmpty()) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException ex) {
          log.error("Interrupted while waiting for shutdown to complete", ex);
        }
      }
      //wait the queue polling time, by which all worker threads should have terminated
      try {
        Thread.sleep(THREAD_SHUTDOWN_CHECK_INTERVAL);
      } catch (InterruptedException ex) {
        log.error("Interrupted while waiting for shutdown to complete", ex);
      }
    }
    long methodEndTime = System.nanoTime();
    log.info("MultiThreadedCacheListener stop method took {} ms", ((methodEndTime - methodStartTime) / 1000000));
  }
  
  /**
   * For management purposes.
   * @return the size of the thread pool for this listener
   */  
  public int getActiveThreadPoolNumber() {
    return executor.getActiveCount();
  }
  
  /**
   * For management purposes.
   * @return the size of the task queue for this listener
   */  
  public int getTaskQueueSize() {
    return taskQueue.size();
  }

  /**
   * For passing an object and the method that needs calling.
   * @author Mark Brightwell
   *
   */
  private final class ObjectAndMethod {
    
    /**
     * Object in notification.
     */
    private T cacheable;
    
    /**
     * Method to call.
     */
    private SupportedMethods method;

    /**
     * Constructor.
     * @param cacheable the object
     * @param method the method
     */
    private ObjectAndMethod(final T cacheable, final SupportedMethods method) {
      super();
      this.cacheable = cacheable;
      this.method = method;
    }
        
  }
  
  /**
   * The task submitted to the executor threads. One task is submitted per
   * thread and it runs until the server shutdown.
   * 
   * <p>The task simply listens to the {@link LinkedBlockingQueue} field
   * for updates.
   * 
   * <p>Once a shutdown request is received, it will stop processing updates within 
   * @author Mark Brightwell
   *
   */
  private class NotifyTask implements Runnable {
    
    /**
     * Runs from start up to shutdown and listens for updates.
     */
    @Override
    public void run() {      
      while (!shutdownRequestMade) {
        try {          
          ObjectAndMethod objectAndMethod = taskQueue.poll(THREAD_SHUTDOWN_CHECK_INTERVAL, TimeUnit.MILLISECONDS);          
          if (objectAndMethod != null) {
            callCorrectMethod(objectAndMethod);
          }          
        } catch (InterruptedException e) {
          log.warn("Cache Listener thread interrupted in MultiThreadedListener.", e);
        }                         
      }
      
      //empty the queue before shutting down
      ObjectAndMethod objectAndMethod;      
      while ((objectAndMethod = taskQueue.poll()) != null) {
        callCorrectMethod(objectAndMethod);
      }
    }
    
    /**
     * Calls the correct method as recorded in the local map.
     * @param objectAndMethod cacheable with method info
     */
    private void callCorrectMethod(final ObjectAndMethod objectAndMethod) {
      try {
        SupportedMethods method = objectAndMethod.method;
        if (method.equals(SupportedMethods.ON_UPDATE)) {
          c2monCacheListener.notifyElementUpdated(objectAndMethod.cacheable);
        } else {
          c2monCacheListener.confirmStatus(objectAndMethod.cacheable);
        }
      } catch (Exception e) {
        log.error("Exception caught when notifying listener: the update could not be processed.", e);
      }           
    }
  }
}
