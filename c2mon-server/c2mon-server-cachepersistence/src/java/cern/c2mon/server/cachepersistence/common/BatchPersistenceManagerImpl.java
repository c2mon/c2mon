/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.cachepersistence.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.transaction.TransactionTimedOutException;

import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.shared.common.Cacheable;

/**
 * Standard implementation of a {@link BatchPersistenceManager}.
 * 
 * <p>This implementation saves the values of the indicated tags
 * in batches using a {@link CachePersistenceDAO}.
 * 
 * <p>If an exception occurs during the DB persistence, the class
 * will attempt to save them next time around. If the server is 
 * shutdown/crashes with the DB available, this list is lost.
 * 
 * @author Mark Brightwell
 *
 * @param <T> the type held by the given cache
 */
@ManagedResource
public class BatchPersistenceManagerImpl<T extends Cacheable> implements BatchPersistenceManager, SmartLifecycle {
  
  /**
   * Private class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(BatchPersistenceManagerImpl.class);
  
  /**
   * Size of the batches between database commits. Also corresponds
   * to the size of the tasks executed by the ExecutorService.
   */
  private static final int RECORDS_PER_BATCH = 500;
  
  /**
   * Timeout in milliseconds for a single batch to persist.
   */
  private int timeoutPerBatch = 8000;
    
  /**
   * Reference to DAO.
   */
  private CachePersistenceDAO<T> cachePersistenceDAO;
  
  /**
   * Reference to the cache where the cache objects can be retrieved
   * (one of the only references to the cache module from the cache
   * persistence module). Needs setting in constructor.
   */
  private C2monCache<Long, T> cache;
  
  /**
   * Autowired as always the same singleton.
   */
  @Autowired
  private ClusterCache clusterCache;
  
  /**
   * Set of tags that the server failed to persist successfully and
   * need persisting when the connection is back. This set should
   * not grow indefinitely since it only keeps the ids of the 
   * elements to persist (so max size is size of cache).
   * 
   * <p>In the case of a server crash or forced shutdown, or if 
   * shutting down the server while the DB is unavailable, these
   * may not be persisted (leaving the cache and DB inconsistent
   * in the case of a distributed cache). Refresh from DAQs is then
   * required to fix the inconsistency (TODO add this as JMX call to
   * cache).
   * 
   * <p>Not shared across servers or kept in distributed cache (could
   * be done at later stage to avoid inconsistencies, with server updating
   * DB once it is back).
   */
  private Set<Long> toBePersisted = new HashSet<Long>();
  
  /**
   * Lock for accessing toBePersisted collection, used only
   * within this class. The lock is to make the bean thread-safe,
   * in case it gets called on multiple threads (which is not
   * currently the case).
   */
  private ReentrantReadWriteLock toBePersistedLock = new ReentrantReadWriteLock();
  
  /**
   * Executor running the persistence tasks.
   */
  private ThreadPoolExecutor persistenceExecutor = new ThreadPoolExecutor(1, 1, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000));
  
  /**
   * Is the Spring component started.
   */
  private boolean started = false;
  
  /**
   * Constructor required cache and the persistence bean for this cache.
   * 
   * @param cachePersistenceDAO the mapper bean for this cache
   * @param cache the cache that is being persisted
   */
  public BatchPersistenceManagerImpl(final CachePersistenceDAO<T> cachePersistenceDAO, final C2monCache<Long, T> cache) {
    super();
    this.cachePersistenceDAO = cachePersistenceDAO;
    this.cache = cache;
  }

  @Override  
  public void persistList(final Collection<Long> keyCollection) {
    clusterCache.acquireWriteLockOnKey(cachePersistenceLock);
    try {
      LOGGER.debug("Submitting new persistence task (currently " + persistenceExecutor.getQueue().size() + " tasks in queue)");
      
      //local set, no synch needed; removes duplicates from collection (though unnecessary with current SynchroBuffer)
      Set<Long> localToBePersisted = new HashSet<Long>(keyCollection);
      
      toBePersistedLock.writeLock().lock();
      try {
        localToBePersisted.addAll(toBePersisted);  //gets rid of all duplicates
        toBePersisted.clear();
      } finally {
        toBePersistedLock.writeLock().unlock();
      }
      
      int size = localToBePersisted.size();
      
      LOGGER.debug("Persisting " + size + " cache object(s) to the database (" + cache.getClass() + ")");      
      
      LinkedList<Future< ? >> taskResults = new LinkedList<Future< ? >>();
      Map<Future< ? >, Collection<Long>> submittedSets = new HashMap<Future<?>, Collection<Long>>();
      
      Iterator<Long> it = localToBePersisted.iterator();      
      while (it.hasNext()) {
        PersistenceTask task = new PersistenceTask();
        LinkedList<Long> persistedIds = new LinkedList<Long>();
        int counter = 0;
        while (it.hasNext() && counter < RECORDS_PER_BATCH) {
          Long currentId = it.next();
          task.put(currentId);          
          counter++;
          persistedIds.push(currentId);
        }
        Future< ? > result = persistenceExecutor.submit(task);
        taskResults.offerLast(result);
        submittedSets.put(result, persistedIds);
      }    
      
      //wait for all to complete; if wait longer than 5s for a single 
      //then all tasks will be rerun
     
      
      int count = 0;
      int exceptionCount = 0;
      for (Future< ? > result : taskResults) {
        boolean exceptionCaught = false;
        count++;
        try {
          result.get(timeoutPerBatch, TimeUnit.MILLISECONDS);
          LOGGER.debug("Persistence batch number " + count + " completed."); 
        } catch (InterruptedException e) {
          LOGGER.error("Interrupted exception caught when waiting for persistence task " + count + " to complete", e);
          exceptionCaught = true;   
        } catch (ExecutionException e) {
          LOGGER.error("ExecutionException thrown when executing cache persistence task " + count + "; original cause is ", e.getCause());          
          exceptionCaught = true;        
        } catch (TimeoutException e) {
          LOGGER.warn("Timeout while waiting for persistence task " + count + " to "
              + "complete (timeout per batch of " + RECORDS_PER_BATCH + " is set at " + timeoutPerBatch + " milliseconds; cancelling batch)"
              + "Cache elements will be persisted during next persistence task.", e);          
          result.cancel(true);
          exceptionCaught = true;   
        } finally {
          if (exceptionCaught) {                        
            exceptionCount++;
            toBePersistedLock.writeLock().lock();
            try {
              toBePersisted.addAll(submittedSets.get(result));
            } finally {
              toBePersistedLock.writeLock().unlock();
            }
          }
        }
      } 
      if (exceptionCount == 0) {
        LOGGER.debug("Completed persistence of all " + count + " batches");
      } else {
        LOGGER.debug(exceptionCount + " out of " + count + " persistence batches failed and will be resubmitted.");
      }
    } finally {
      clusterCache.releaseWriteLockOnKey(cachePersistenceLock);
    }
  }
  
  @Override
  public void addElementToPersist(Long key) {
    toBePersistedLock.writeLock().lock();
    try {
      toBePersisted.add(key);
    } finally {
      toBePersistedLock.writeLock().unlock();
    }
  }
  
  @ManagedOperation(description = "Persists the current cache contents to the DB (cache persistence). Ensures cache object runtime values & DB are synchronized.")
  public void persistAllCacheToDatabase() {
    persistList(cache.getKeys());
  }

  /**
   * Sets the timeout in milliseconds for a single batch to persist.
   * Default is 8s.
   * 
   * @param timeoutPerBatch in milliseconds
   */
  public void setTimeoutPerBatch(final int timeoutPerBatch) {
    this.timeoutPerBatch = timeoutPerBatch;
  }
  
  /**
   * Task persisting a collection of RECORDS_PER_BATCH cache 
   * objects to the database, by calling a CachePersistenceDAO.
   * 
   * <p>Exceptions are caught by the ExecutorService, and wrapped and thrown
   * when calling get on the Future.
   * 
   * @author Mark Brightwell
   *
   */
  class PersistenceTask implements Callable<Object> {

    /**
     * Keys of cache elements to persist.
     */
    private ArrayList<Long> keyList = new ArrayList<Long>(RECORDS_PER_BATCH);

    /**
     * Add the key to the task, prior to execution.
     * @param key the cache element key
     */
    public void put(final Long key) {
      keyList.add(key);
    }

    /**
     * Retrieves the cache elements and persists them.
     * Requires a new transaction.
     */
    @Override    
    public Object call() {
      cachePersistenceDAO.persistBatch(keyList);
      return null;
    }

  }

  /**
   * Starts up automatically on context creation.
   */
  @Override
  public boolean isAutoStartup() {
    return false;
  }
  
  @Override
  public void stop(Runnable callback) {
    stop();
    callback.run();
  }

  @Override
  public synchronized boolean isRunning() {
    return started;
  }

  @Override
  public synchronized void start() {
    started = true;
  }

  /**
   * Will not shutdown correctly until all elements can be persisted.
   */
  @Override
  public synchronized void stop() {    
    LOGGER.info("Shutting down cache persistence manager (" + cache.getClass().getSimpleName() + ")");
    started = false;
    persistenceExecutor.shutdown();
    //may be none-empty if added using addElementToPersist
    while (!toBePersisted.isEmpty()) {
      LOGGER.debug("Detected cache objects that need persisting... trying to persist them.");
      toBePersistedLock.writeLock().lock();
      try {
        cachePersistenceDAO.persistBatch(new ArrayList<Long>(toBePersisted));
        toBePersisted.clear();
      } catch (PersistenceException e) {
        LOGGER.error("Exception caught while persisting final batch of cache objects - will try again in 1s", e);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e1) {          
          LOGGER.error("Interrupted during sleep", e1);
        }
      } finally {
        toBePersistedLock.writeLock().unlock();      
      }
    }           
  }

  @Override
  public int getPhase() {    
    return ServerConstants.PHASE_STOP_LAST;
  }

}
