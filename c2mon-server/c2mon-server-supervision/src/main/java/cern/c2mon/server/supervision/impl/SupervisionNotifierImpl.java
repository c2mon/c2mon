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
package cern.c2mon.server.supervision.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.component.ExecutorLifecycleHandle;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.supervision.SupervisionListener;
import cern.c2mon.server.supervision.SupervisionNotifier;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cern.c2mon.shared.common.CacheEvent.CONFIRM_STATUS;
import static cern.c2mon.shared.common.CacheEvent.SUPERVISION_CHANGE;

/**
 * Notifies the all the listeners of changes in the
 * supervision status of the DAQs/Equipment/Subequipment.
 *
 * <p>Listeners can register on multiple threads and should
 * ensure they can keep pace with the supervision notifications
 * or else they may slow down the server operation (since
 * the notification thread may be frozen). This should
 * be tuned using the queue size and thread number. In particular,
 * the listener does not need to start multiple threads itself.
 *
 * @author Mark Brightwell
 */
@Slf4j
@Service("supervisionNotifier")
@ManagedResource(objectName = "cern.c2mon:name=supervisionNotifier")
public class SupervisionNotifierImpl implements SupervisionNotifier {

  /**
   * Default size of task queue for a listener executor.
   */
  private static final int DEFAULT_QUEUE_SIZE = Integer.MAX_VALUE;

  /**
   * Default executor thread timeout.
   */
  private static final int DEFAULT_THREAD_TIMEOUT = 120;

  /**
   * Default number of threads on which a listener will be called.
   * Each listener gets his own thread.
   */
  private static final int DEFAULT_NUMBER_THREADS = 1;


  /**
   * List of listeners + lock for access.
   */
  private List<SupervisionListener> supervisionListeners = new ArrayList<SupervisionListener>();
  private ReentrantReadWriteLock listenerLock = new ReentrantReadWriteLock();

  /**
   * Map of executors. one for each listener.
   */
  private Map<SupervisionListener, ThreadPoolExecutor> executors = new HashMap<SupervisionListener, ThreadPoolExecutor>();
  private C2monCache<SupervisionStateTag> stateTagCache;


  /**
   * Constructor.
   */
  @Autowired
  public SupervisionNotifierImpl(final C2monCache<SupervisionStateTag> stateTagCache) {
    super();
    this.stateTagCache = stateTagCache;
  }

  @PostConstruct
  public void init() {
    stateTagCache.getCacheListenerManager()
      .registerListener(this::notifyElementUpdated
        , SUPERVISION_CHANGE, CONFIRM_STATUS);
  }


  private void notifyElementUpdated(SupervisionStateTag stateTag) {
    Timestamp supervisionTime;
    String supervisionMessage;
    if (stateTag.getStatusTime() != null) {
      supervisionTime = stateTag.getStatusTime();
    } else {
      supervisionTime = new Timestamp(System.currentTimeMillis());
    }
    if (stateTag.getStatusDescription() != null) {
      supervisionMessage = stateTag.getStatusDescription();
    } else {
      supervisionMessage = stateTag.getSupervisedEntity() + " " + stateTag.getName() + " is " + stateTag.getSupervisionStatus();
    }
    notifySupervisionEvent(new SupervisionEventImpl(stateTag.getSupervisedEntity(),
      stateTag.getId(), stateTag.getName(), stateTag.getSupervisionStatus(),
      supervisionTime,
      supervisionMessage));
  }

  @Override
  public Lifecycle registerAsListener(final SupervisionListener supervisionListener) {
    listenerLock.writeLock().lock();
    try {
      ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(DEFAULT_NUMBER_THREADS, DEFAULT_NUMBER_THREADS,
        DEFAULT_THREAD_TIMEOUT, TimeUnit.SECONDS, new LinkedBlockingQueue<>(DEFAULT_QUEUE_SIZE),
        new ThreadPoolExecutor.AbortPolicy());
      threadPoolExecutor.setThreadFactory(r -> {
        StringBuilder builder = new StringBuilder();
        builder.append("Supervision-").append(executors.size()).append("-").append(threadPoolExecutor.getActiveCount());
        return new Thread(r, builder.toString());
      });
      threadPoolExecutor.allowCoreThreadTimeOut(true);
      executors.put(supervisionListener, threadPoolExecutor);
      supervisionListeners.add(supervisionListener);
      return new ExecutorLifecycleHandle(threadPoolExecutor);
    } finally {
      listenerLock.writeLock().unlock();
    }
  }

  @Override
  public void notifySupervisionEvent(final SupervisionEvent supervisionEvent) {
    log.debug("Notifying listeners of Supervision Event: " + supervisionEvent.getEntity() + " " + supervisionEvent.getEntityId() + " is " + supervisionEvent.getStatus());

    for (SupervisionListener listener : supervisionListeners) {
      executors.get(listener).execute(new SupervisionNotifyTask(supervisionEvent, listener));
    }
  }

  /**
   * For management purposes.
   *
   * @return the size of the queues of the various supervison listeners.
   */
  @ManagedOperation(description = "Get listener queue sizes.")
  public List<Integer> getQueueSizes() {
    ArrayList<Integer> queueSizes = new ArrayList<Integer>();
    listenerLock.writeLock().lock();
    try {
      for (SupervisionListener listener : supervisionListeners) {
        queueSizes.add(executors.get(listener).getQueue().size());
      }
    } finally {
      listenerLock.writeLock().unlock();
    }
    return queueSizes;
  }

  /**
   * For management purposes.
   *
   * @return the number of active threads for each listener
   */
  @ManagedOperation(description = "Get listener active threads.")
  public List<Integer> getNumActiveThreads() {
    ArrayList<Integer> activeThreads = new ArrayList<>();
    listenerLock.writeLock().lock();
    try {
      for (SupervisionListener listener : supervisionListeners) {
        activeThreads.add(executors.get(listener).getActiveCount());
      }
    } finally {
      listenerLock.writeLock().unlock();
    }
    return activeThreads;
  }

  /**
   * Notifies a listener of a given event.
   *
   * @author Mark Brightwell
   */
  private class SupervisionNotifyTask implements Runnable {

    /**
     * The event to pass to the listener
     */
    private SupervisionEvent event;

    /**
     * The listener to call.
     */
    private SupervisionListener listener;

    /**
     * Constructor.
     *
     * @param event    the event
     * @param listener the listener
     */
    public SupervisionNotifyTask(final SupervisionEvent event, final SupervisionListener listener) {
      super();
      this.event = event;
      this.listener = listener;
    }

    /**
     * Calls the listener with the event as parameter.
     */
    @Override
    public void run() {
      try {
        listener.notifySupervisionEvent(event);
      } catch (RuntimeException e) {
        log.error("Exception caught while notifying supervision event: the supervision status will no longer be correct and needs refreshing!", e);
      }
    }

  }

}
