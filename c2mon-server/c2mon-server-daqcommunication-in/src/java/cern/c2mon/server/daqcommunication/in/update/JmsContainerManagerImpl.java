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
package cern.c2mon.server.daqcommunication.in.update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import javax.jms.Message;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.daqcommunication.in.JmsContainerManager;

/**
 * Implementation of the JmsContainer bean. Also manages the daq-in update
 * components, including shutting down the daq-in JMS connection factory.
 * 
 * @author Mark Brightwell
 *
 */
@ManagedResource(objectName="cern.c2mon:name=processJmsContainerManager")
public class JmsContainerManagerImpl implements JmsContainerManager, SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(JmsContainerManagerImpl.class);
  
  /**
   * Milliseconds before an idle consumer thread is closed (not used by any Process container).
   */
  private static final long THREAD_IDLE_LIMIT = 60000;  
  
  /**
   * Flag for lifecycle management.
   */
  private volatile boolean running = false;
  
  /**
   * Reference to Process cache.
   */
  private ProcessCache processCache;
  
  /**
   * Running containers.
   */
  private ConcurrentHashMap<Long, DefaultMessageListenerContainer> jmsContainers = 
                          new ConcurrentHashMap<Long, DefaultMessageListenerContainer>();
  
  /**
   * Threads shared by all containers.
   */
  private ThreadPoolExecutor threadPool;
  
  /**
   * The JMS connection factory used (instantiated in XML).
   */
  private ConnectionFactory updateConnectionFactory;
  
  /**
   * The message listener: is SourceUpdateManagerImpl
   */
  private SessionAwareMessageListener<Message> listener; 
  
  /**
   * The number of initial consumer threads.
   */
  private int consumersInitial = 1;
  
  /**
   * The number of initial consumer threads.
   */
  private int consumersMax = 1;
  
  /**
   * The max number of consumer threads.
   */
  private boolean sessionTransacted = false;
   
  /**
   * The trunk of the queue name used for all Processes.
   */
  private String jmsUpdateQueueTrunk = "default.update.trunk";
  
  /**
   * Warm up time, after which multiple threads are allowed for single Process
   * (introduced to improve performance when multiple servers available).
   */
  private int updateWarmUpSeconds = 10;

  /**
   * Container listener max messages per task.
   */
  private int maxMessages;

  /**
   * Container listener receive timeout.
   */
  private long receiveTimeout;

  /**
   * Container listener idle task execution limit.
   */
  private int idleTaskExecutionLimit;

  /**
   * Number of threads executing receive calls.
   */
  private int nbExecutorThreads;
  
  /**
   * Timer for checking if subscription to JMS have been modified by
   * another cluster member.
   */
  private Timer subscriptionChecker;
  
  /**
   * For accessing reconfiguration lock.
   */
  private ClusterCache clusterCache;
  
  /**
   * How often does the subscription checker run.
   */
  private static final long SUBSCRIPTION_CHECK_INTERVAL = 120000L;

  
  /**
   * Constructor.
   */
  public JmsContainerManagerImpl(final ProcessCache processCache, 
                                final @Qualifier("daqInConnectionFactory") ConnectionFactory updateConnectionFactory,
                                final @Qualifier("sourceUpdateManager") SessionAwareMessageListener<Message> listener,
                                final @Qualifier("clusterCache") ClusterCache clusterCache) {
    super();
    this.processCache = processCache;
    this.updateConnectionFactory = updateConnectionFactory;
    this.listener = listener;
    this.clusterCache = clusterCache;
  }
 

  @PostConstruct
  public void init() {
    threadPool = new ThreadPoolExecutor(nbExecutorThreads, nbExecutorThreads, 
        THREAD_IDLE_LIMIT, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    for (Long id : processCache.getKeys()) {
      subscribe(processCache.get(id), consumersInitial);
    }    
    threadPool.allowCoreThreadTimeOut(true);
  }
  
  @Override
  public void subscribe(final Process process) { 
    LOGGER.trace("Subscribing to updates from Process " + process.getId());
    if (!jmsContainers.containsKey(process.getId())) {
      DefaultMessageListenerContainer container = subscribe(process, this.consumersMax);   
      container.start();
    } else {
      LOGGER.warn("Attempt at creating a JMS listener container for a Process that already has one.");
    }        
  }
  
  /**
   * Returns the container so that it can be started manually when added during
   * server runtime.
   * @param process the Process to create a container for
   * @param consumersMax the max number of consumers (at start-up subscribe with less)
   * @return the JMS container that was created
   */
  private DefaultMessageListenerContainer subscribe(final Process process, final int consumersMax) {   
    DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
    container.setConnectionFactory(updateConnectionFactory);
    container.setDestination(new ActiveMQQueue(jmsUpdateQueueTrunk + "." + process.getName()));
    container.setMessageListener(listener);
    container.setConcurrentConsumers(consumersInitial);
    container.setMaxConcurrentConsumers(consumersMax);
    container.setSessionTransacted(sessionTransacted);
    container.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONSUMER);
    container.setAutoStartup(false);
    container.setPhase(ServerConstants.PHASE_START_LAST);
    container.setMaxMessagesPerTask(maxMessages);
    container.setReceiveTimeout(receiveTimeout);
    container.setIdleTaskExecutionLimit(idleTaskExecutionLimit);
    container.setBeanName(process.getName() + " update JMS container");
    container.setTaskExecutor(threadPool); 
    container.setAcceptMessagesWhileStopping(false);
    jmsContainers.put(process.getId(), container);
    container.initialize();
    return container;
  }

  @Override
  public void unsubscribe(final Process process) {
    unsubscribe(process.getId());
  }
  
  /**
   * Unsubscribes JMS container for the given process id, if this one
   * can be found (o.w. does nothing).
   * @param processId if of the process
   */
  private void unsubscribe(final Long processId) {
    LOGGER.trace("Unsubscribing from updates for Process " + processId);
    if (jmsContainers.containsKey(processId)) {
      DefaultMessageListenerContainer container = jmsContainers.get(processId);      
      container.shutdown();      
      jmsContainers.remove(processId);
    } else {
      LOGGER.warn("Attempt to remove an unrecognized JMS listener container.");
    }
  }
  
  /**
   * For management purposes. Starts the JMS container.
   * @param processName name of the process
   */
  @ManagedOperation(description="Stop this JMS container")
  public void stopContainer(String processName) {
    LOGGER.info("Stopping JMS container for Process " + processName);
    jmsContainers.get(processCache.getProcessId(processName)).stop();
  }
  
  /**
   * For management purposes. Stops the JMS container.
   * @param processName name of the process
   */
  @ManagedOperation(description="Start this JMS container.")
  public void startContainer(String processName) {
    LOGGER.info("Starting JMS container for Process " + processName);
    jmsContainers.get(processCache.getProcessId(processName)).start();
  }
  
  /**
   * For management purposes. Returns the queue size of receive tasks
   * waiting for execution.
   * @return queue size
   */
  @ManagedOperation(description="Get executor queue size.")
  public int getTaskQueueSize() {
    return threadPool.getQueue().size();
  }
  
  /**
   * For management purposes. Returns the number of active threads listening
   * for JMS updates.
   * @return the number of active threads
   */
  @ManagedOperation(description="Get the number of active threads listening for JMS updates.")
  public int getNumActiveThreads() {
    return threadPool.getActiveCount();
  }


  /**
   * Will only be used at start up.
   */
  @Override
  public synchronized boolean isRunning() {    
    return running;     
  }

  /**
   * Increases the max number of update threads to the value set in the properties file after 2 minutes (initial set in XML).
   * Only intended to be started/stopped once (multiple calls to start will have no effect).
   */
  @Override
  public synchronized void start() {
    if (!running) {
      running = true;       
      //start JMS containers (not in Spring context!)
      LOGGER.info("Starting Process JMS listeners...");
      for (Map.Entry<Long, DefaultMessageListenerContainer> entry : jmsContainers.entrySet()) {
        entry.getValue().start();      
      }
      LOGGER.info("Finished starting Process JMS listeners.");
      
      //start thread that will increase the listener thread number after warm up time
      //(this thread expires if stop is called)
      new Thread(new Runnable() {
        
        @Override
        public void run() {
          int counter = 0;
          try {
            while (counter < updateWarmUpSeconds && running) {            
              Thread.sleep(1000);            
              counter++;
            }
          } catch (InterruptedException e) {
            LOGGER.error("Interrupted during warm-up phase; starting all listener threads.", e);
          }
          if (running) {
            LOGGER.info("Increasing max concurrent update consumers to operational value.");
            for (Map.Entry<Long, DefaultMessageListenerContainer> entry : jmsContainers.entrySet()) {
              entry.getValue().setMaxConcurrentConsumers(consumersMax);      
            }
          }        
        }
        
      }).start(); 
      
      //start thread that will periodically check if a Process has been added or removed from a distributed cluster
      subscriptionChecker = new Timer();
      subscriptionChecker.schedule(new SubscriptionCheck(), SUBSCRIPTION_CHECK_INTERVAL, SUBSCRIPTION_CHECK_INTERVAL);
    }    
  }

  //TODO increase JMS retries in ActiveMQ to > #consumers in one server (ow may not get picked up by other server)
  /**
   * Permanent shutdown.
   */
  @Override
  public synchronized void stop() {       
    try {      
      LOGGER.info("Stopping JMS update containers listening for tag updates from the DAQ layer.");
      subscriptionChecker.cancel();
      ThreadPoolExecutor shutdownExecutor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
      Collection<ContainerShutdownTask> containerTasks = new ArrayList<ContainerShutdownTask>();      
      for (Map.Entry<Long, DefaultMessageListenerContainer> entry : jmsContainers.entrySet()) {
        ContainerShutdownTask containerShutdownTask = new ContainerShutdownTask(entry.getValue());
        containerTasks.add(containerShutdownTask);
      }
      shutdownExecutor.invokeAll(containerTasks, 60, TimeUnit.SECONDS);
      shutdownExecutor.shutdown();
      jmsContainers.clear();
      threadPool.shutdown();      
//      LOGGER.info("Stopping JMS connections to DAQs");
//      updateConnectionFactory.stop(); //closes all JMS connections in the pool      
    } catch (Exception e) {
      LOGGER.error("Exception caught while closing down the Spring listener/JMS thread pool", e);     
    }   
  }

  @Override
  public boolean isAutoStartup() {  
    return false;
  }

  @Override
  public synchronized void stop(final Runnable runnable) {
    running = false;
    stop();
    runnable.run();
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST;
  }


  /**
   * @param consumersInitial the consumersInitial to set
   */
  @Override
  public void setConsumersInitial(final int consumersInitial) {
    this.consumersInitial = consumersInitial;
  }


  /**
   * @param consumersMax the consumersMax to set
   */
  @Override
  public void setConsumersMax(final int consumersMax) {
    this.consumersMax = consumersMax;
  }


  /**
   * @param sessionTransacted the sessionTransacted to set
   */
  @Override
  public void setSessionTransacted(final boolean sessionTransacted) {
    this.sessionTransacted = sessionTransacted;
  }


  /**
   * @param jmsUpdateQueueTrunk the jmsUpdateQueueTrunk to set
   */
  @Override
  public void setJmsUpdateQueueTrunk(final String jmsUpdateQueueTrunk) {
    this.jmsUpdateQueueTrunk = jmsUpdateQueueTrunk;
  }


  /**
   * @param updateWarmUpSeconds the updateWarmUpSeconds to set
   */
  @Override
  public void setUpdateWarmUpSeconds(final int updateWarmUpSeconds) {
    this.updateWarmUpSeconds = updateWarmUpSeconds;
  }


  /**
   * Set the max number of messages in a task.
   * @param maxMessages the maxMessages to set
   */
  public void setMaxMessages(final int maxMessages) {
    this.maxMessages = maxMessages;
  }


  /**
   * Set the timeout on receive calls.
   * @param receiveTimeout the receiveTimeout to set
   */
  public void setReceiveTimeout(final long receiveTimeout) {
    this.receiveTimeout = receiveTimeout;
  }


  /**
   * Set the idle task execution limit.
   * @param idleTaskExecutionLimit the idleTaskExecutionLimit to set
   */
  public void setIdleTaskExecutionLimit(final int idleTaskExecutionLimit) {
    this.idleTaskExecutionLimit = idleTaskExecutionLimit;
  }


  /**
   * Sets the number of threads doing the receive calls.
   * @param nbExecutorThreads the nbExecutorThreads to set
   */
  public void setNbExecutorThreads(final int nbExecutorThreads) {
    this.nbExecutorThreads = nbExecutorThreads;
  }
  
  /**
   * This task checks that the C2MON server JMS process subscriptions
   * are synchronized with the latest cluster state. If not, subscriptions
   * will be added/removed as required.
   * 
   * <p>Currently runs every 2 minutes. Is not allowed during reconfigurations
   * of the system, to prevent clashes during subscription/un-subscription.
   * 
   * @author Mark Brightwell
   *
   */
  private class SubscriptionCheck extends TimerTask {

    @Override
    public void run() {
      clusterCache.acquireWriteLockOnKey(CONFIG_LOCK_KEY);
      try {
        LOGGER.debug("Checking JMS subscriptions are up to date.");
        try {
          //check no new 
          for (Long id : processCache.getKeys()) {
            if (!jmsContainers.containsKey(id)) {
              subscribe(processCache.get(id));              
            }        
          } 
          //check no old that needs unsubscribing
          for (Map.Entry<Long, DefaultMessageListenerContainer> entry : jmsContainers.entrySet()) {
            if (!processCache.getKeys().contains(entry.getKey())) {
              unsubscribe(entry.getKey());     
            }
          }
        } catch (Exception e) {
          LOGGER.error("Unexpected exception caught while updating Process JMS containers", e);
        }
      } finally {
        clusterCache.releaseWriteLockOnKey(CONFIG_LOCK_KEY);
      }                
    }
  }
  
  /**
   * For shutting down many containers.
   */
  private class ContainerShutdownTask implements Callable<DefaultMessageListenerContainer> {

    private DefaultMessageListenerContainer container;
    
    /**
     * Constructor.
     * @param container that needs shutting down
     */
    public ContainerShutdownTask(DefaultMessageListenerContainer container) {
      super();
      this.container = container;
    }

    @Override
    public DefaultMessageListenerContainer call() throws Exception {
      container.shutdown();
      return container;
    }
    
  }
}
