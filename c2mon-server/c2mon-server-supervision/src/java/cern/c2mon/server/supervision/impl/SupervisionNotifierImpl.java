/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.supervision.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.common.component.ExecutorLifecycleHandle;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.server.supervision.SupervisionListener;
import cern.c2mon.server.supervision.SupervisionNotifier;

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
 * <p>Each listener can specify on how many threads it
 * wishes to be notified, using the following property.
 * <ul>
 *  <li>supervision.notification.threads.max NOT IMPLEMENTED YET
 * </ul>
 * 
 * 
 * @author Mark Brightwell
 *
 */
@Service("supervisionNotifier")
@ManagedResource(objectName="cern.c2mon:name=supervisionNotifier")
public class SupervisionNotifierImpl implements SupervisionNotifier, C2monCacheListener<Supervised> {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(SupervisionNotifierImpl.class); 
  
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
   * Caches for getting supervision status changes.
   */
  private ProcessCache processCache;
  private EquipmentCache equipmentCache;
  private SubEquipmentCache subEquipmentCache;
  
  
  /**
   * List of listeners + lock for access.
   */
  private List<SupervisionListener> supervisionListeners = new ArrayList<SupervisionListener>();
  private ReentrantReadWriteLock listenerLock = new ReentrantReadWriteLock();
  
  /**
   * Map of executors. one for each listener.
   */
  private Map<SupervisionListener, ThreadPoolExecutor> executors = new HashMap<SupervisionListener, ThreadPoolExecutor>();    
    
  
  /**
   * Constructor.
   * @param processCache process cache
   * @param equipmentCache equipment cache
   * @param subEquipmentCache subequipment cache
   */
  @Autowired
  public SupervisionNotifierImpl(final ProcessCache processCache, 
                                final EquipmentCache equipmentCache, 
                                final SubEquipmentCache subEquipmentCache) {
    super();
    this.processCache = processCache;
    this.equipmentCache = equipmentCache;
    this.subEquipmentCache = subEquipmentCache;
  }
  
  @PostConstruct
  public void init() {
    processCache.registerSynchronousListener(this);
    equipmentCache.registerSynchronousListener(this);
    subEquipmentCache.registerSynchronousListener(this);
  }


  @Override
  public Lifecycle registerAsListener(final SupervisionListener supervisionListener) {
    return registerAsListener(supervisionListener, DEFAULT_NUMBER_THREADS);
  }
  
  
  @Override
  public Lifecycle registerAsListener(final SupervisionListener supervisionListener, final int numberThreads) {
   return registerAsListener(supervisionListener, numberThreads, DEFAULT_QUEUE_SIZE);
  }
  
  /**
   * No synchronisation necessary as all added at start up.
   * @param supervisionListener the listener that should be notified of supervision changes
   * @param numberThreads the number of threads <b>this</b> listener should be notified on (max = core); core threads also time out
   * @param queueSize the size of the queue to use for queuing supervision events (should be set according to
   *  number of DAQs/Equipments and the length of the expected tasks; runtime exception thrown if queue fills up!) 
   */
  @Override
  public Lifecycle registerAsListener(final SupervisionListener supervisionListener, final int numberThreads, final int queueSize) {
    listenerLock.writeLock().lock();
    try {
      ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(numberThreads, numberThreads, DEFAULT_THREAD_TIMEOUT, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(queueSize), new ThreadPoolExecutor.AbortPolicy());
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
    LOGGER.debug("Notifying listeners of Supervision Event: " + supervisionEvent.getEntity() + " " + supervisionEvent.getEntityId() + " is " + supervisionEvent.getStatus());
    listenerLock.writeLock().lock();
    try {
      for (SupervisionListener listener : supervisionListeners) {
        executors.get(listener).execute(new SupervisionNotifyTask(supervisionEvent, listener));
      }
    } finally {
      listenerLock.writeLock().unlock();
    }    
  }
  
  @Override
  public void notifyElementUpdated(Supervised supervised) {
    Timestamp supervisionTime;
    String supervisionMessage;
    if (supervised.getStatusTime() != null) {
      supervisionTime = supervised.getStatusTime();
    } else {
      supervisionTime = new Timestamp(System.currentTimeMillis());
    }
    if (supervised.getStatusDescription() != null) {
      supervisionMessage = supervised.getStatusDescription();
    } else {
      supervisionMessage = supervised.getSupervisionEntity() + " " + supervised.getName() + " is " + supervised.getSupervisionStatus();
    }
    notifySupervisionEvent(new SupervisionEventImpl(supervised.getSupervisionEntity(), 
                                                    supervised.getId(), supervised.getSupervisionStatus(), 
                                                    supervisionTime, 
                                                    supervisionMessage));    
  }
  
  @Override
  public void confirmStatus(Supervised supervised) {
    notifyElementUpdated(supervised);
  }
  
  /**
   * For management purposes.
   * @return the size of the queues of the various supervison listeners.
   */
  @ManagedOperation(description="Get listener queue sizes.")
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
   * @return the number of active threads for each listener
   */
  @ManagedOperation(description="Get listener active threads.")
  public List<Integer> getNumActiveThreads() {
    ArrayList<Integer> activeThreads = new ArrayList<Integer>();
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
   *
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
     * @param event the event
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
        LOGGER.error("Exception caught while notifying supervision event: the supervision status will no longer be correct and needs refreshing!", e);
      }      
    }
    
  }
  
}
