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
package cern.c2mon.server.cachepersistence.common;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
@Slf4j
@ManagedResource
public class BatchPersistenceManagerImpl<T extends Cacheable> implements BatchPersistenceManager, SmartLifecycle {

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
  private C2monCache<T> cache;

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
  private Set<Long> toBePersisted = new HashSet<>();

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
  private ThreadPoolTaskExecutor cachePersistenceThreadPoolTaskExecutor;

  private boolean started = false;

  public BatchPersistenceManagerImpl(final CachePersistenceDAO<T> cachePersistenceDAO, final C2monCache<T> cache,
                                     ThreadPoolTaskExecutor threadPoolTaskExecutor) {
    super();
    this.cachePersistenceDAO = cachePersistenceDAO;
    this.cache = cache;
    this.cachePersistenceThreadPoolTaskExecutor = threadPoolTaskExecutor;
  }

  @Override
  public void persistList(final Collection<Long> keyCollection) {
    log.debug("Submitting new persistence task (currently " + cachePersistenceThreadPoolTaskExecutor.getThreadPoolExecutor().getQueue().size() + " tasks in queue)");

    //local set, no synch needed; removes duplicates from collection (though unnecessary with current SynchroBuffer)
    Set<Long> localToBePersisted = new HashSet<>(keyCollection);

    toBePersistedLock.writeLock().lock();
    try {
      localToBePersisted.addAll(toBePersisted);  //gets rid of all duplicates
      toBePersisted.clear();
    } finally {
      toBePersistedLock.writeLock().unlock();
    }

    int size = localToBePersisted.size();

    log.debug("Persisting " + size + " cache object(s) to the database (" + cache.getClass() + ")");

    LinkedList<Future< ? >> taskResults = new LinkedList<>();
    Map<Future< ? >, Collection<Long>> submittedSets = new HashMap<>();

    Iterator<Long> it = localToBePersisted.iterator();
    while (it.hasNext()) {
      PersistenceTask task = new PersistenceTask();
      LinkedList<Long> persistedIds = new LinkedList<>();
      int counter = 0;
      while (it.hasNext() && counter < RECORDS_PER_BATCH) {
        Long currentId = it.next();
        task.put(currentId);
        counter++;
        persistedIds.push(currentId);
      }
      Future< ? > result = cachePersistenceThreadPoolTaskExecutor.submit(task);
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
        log.debug("Persistence batch number " + count + " completed.");
      } catch (InterruptedException e) {
        log.error("Interrupted exception caught when waiting for persistence task " + count + " to complete", e);
        exceptionCaught = true;
      } catch (ExecutionException e) {
        log.error("ExecutionException thrown when executing cache persistence task " + count + "; original cause is ", e.getCause());
        exceptionCaught = true;
      } catch (TimeoutException e) {
        log.warn("Timeout while waiting for persistence task " + count + " to "
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
      log.debug("Completed persistence of all " + count + " batches");
    } else {
      log.debug(exceptionCount + " out of " + count + " persistence batches failed and will be resubmitted.");
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
    private ArrayList<Long> keyList = new ArrayList<>(RECORDS_PER_BATCH);

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
    return true;
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
    log.info("Shutting down cache persistence manager (" + cache.getClass().getSimpleName() + ")");
    started = false;
    cachePersistenceThreadPoolTaskExecutor.shutdown();
    //may be none-empty if added using addElementToPersist
    while (!toBePersisted.isEmpty()) {
      log.debug("Detected cache objects that need persisting... trying to persist them.");
      toBePersistedLock.writeLock().lock();
      try {
        cachePersistenceDAO.persistBatch(new ArrayList<>(toBePersisted));
        toBePersisted.clear();
      } catch (PersistenceException e) {
        log.error("Exception caught while persisting final batch of cache objects - will try again in 1s", e);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e1) {
          log.error("Interrupted during sleep", e1);
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
