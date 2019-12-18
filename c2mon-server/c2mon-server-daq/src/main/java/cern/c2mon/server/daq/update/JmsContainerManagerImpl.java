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
package cern.c2mon.server.daq.update;

import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.daq.JmsContainerManager;
import cern.c2mon.server.daq.config.DaqProperties;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import java.util.*;
import java.util.concurrent.*;

/**
 * Implementation of the JmsContainer bean. Also manages the daq-in update
 * components, including shutting down the daq-in JMS connection factory.
 *
 * @author Mark Brightwell
 *
 */
@Component
@ManagedResource(objectName="cern.c2mon:name=processJmsContainerManager")
public class JmsContainerManagerImpl implements JmsContainerManager, SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(JmsContainerManagerImpl.class);

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
  private C2monCache<Process> processCache;

  /**
   * Running containers.
   */
  private ConcurrentHashMap<Long, DefaultMessageListenerContainer> jmsContainers =
                          new ConcurrentHashMap<Long, DefaultMessageListenerContainer>();

  /**
   * Threads shared by all containers.
   */
  private ThreadPoolTaskExecutor daqThreadPoolTaskExecutor;

  private ProcessService processService;
  /**
   * The JMS connection factory used (instantiated in XML).
   */
  private ConnectionFactory updateConnectionFactory;

  /**
   * The message listener: is SourceUpdateManagerImpl
   */
  private SessionAwareMessageListener<Message> listener;

  /**
   * Timer for checking if subscription to JMS have been modified by
   * another cluster member.
   */
  private Timer subscriptionChecker;

  /**
   * How often does the subscription checker run.
   */
  private static final long SUBSCRIPTION_CHECK_INTERVAL = 120000L;

  private DaqProperties properties;

  /**
   * Constructor.
   */
  @Autowired
  public JmsContainerManagerImpl(final ProcessService processService,
                                 final @Qualifier("daqInConnectionFactory") ConnectionFactory updateConnectionFactory,
                                 final @Qualifier("sourceUpdateManager") SessionAwareMessageListener<Message> listener,
                                 final ThreadPoolTaskExecutor daqThreadPoolTaskExecutor,
                                 final DaqProperties properties) {
    super();
    this.processCache = processService.getCache();
    this.processService = processService;
    this.updateConnectionFactory = updateConnectionFactory;
    this.listener = listener;
    this.daqThreadPoolTaskExecutor = daqThreadPoolTaskExecutor;
    this.properties = properties;
  }


  @PostConstruct
  public void init() {
    daqThreadPoolTaskExecutor.initialize();
    for (Long id : processCache.getKeys()) {
      subscribe(processCache.get(id), properties.getJms().getUpdate().getMaxConsumers());
    }
  }

  @Override
  public void subscribe(final Process process) {
    LOGGER.trace("Subscribing to updates from Process " + process.getId());
    if (!jmsContainers.containsKey(process.getId())) {
      DefaultMessageListenerContainer container = subscribe(process, properties.getJms().getUpdate().getMaxConsumers());
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
    container.setDestination(new ActiveMQQueue(properties.getJms().getQueuePrefix() + ".update." + process.getName()));
    container.setMessageListener(listener);
    container.setConcurrentConsumers(properties.getJms().getUpdate().getInitialConsumers());
    container.setMaxConcurrentConsumers(consumersMax);
    container.setSessionTransacted(properties.getJms().getUpdate().isTransacted());
    container.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONSUMER);
    container.setAutoStartup(false);
    container.setPhase(ServerConstants.PHASE_START_LAST);
    container.setMaxMessagesPerTask(properties.getJms().getUpdate().getMaxMessagesPerTask());
    container.setReceiveTimeout(properties.getJms().getUpdate().getReceiveTimeout());
    container.setIdleTaskExecutionLimit(properties.getJms().getUpdate().getIdleTaskExecutionLimit());
    container.setBeanName(process.getName() + " update JMS container");
    container.setTaskExecutor(daqThreadPoolTaskExecutor);
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

      // Shut down the container in another thread to avoid blocking
      Executors.newFixedThreadPool(1).submit(new ContainerShutdownTask(container));

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
    jmsContainers.get(processService.getProcessIdFromName(processName).getId()).stop();
  }

  /**
   * For management purposes. Stops the JMS container.
   * @param processName name of the process
   */
  @ManagedOperation(description="Start this JMS container.")
  public void startContainer(String processName) {
    LOGGER.info("Starting JMS container for Process " + processName);
    jmsContainers.get(processService.getProcessIdFromName(processName).getId()).start();
  }

  /**
   * For management purposes. Returns the queue size of receive tasks
   * waiting for execution.
   * @return queue size
   */
  @ManagedOperation(description="Get executor queue size.")
  public int getTaskQueueSize() {
//    return daqThreadPoolTaskExecutor.getQueue().size();
    return daqThreadPoolTaskExecutor.getThreadPoolExecutor().getQueue().size();
  }

  /**
   * For management purposes. Returns the number of active threads listening
   * for JMS updates.
   * @return the number of active threads
   */
  @ManagedOperation(description="Get the number of active threads listening for JMS updates.")
  public int getNumActiveThreads() {
    return daqThreadPoolTaskExecutor.getActiveCount();
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
            while (counter < properties.getJms().getUpdate().getConsumerWarmupTime() && running) {
              Thread.sleep(1000);
              counter++;
            }
          } catch (InterruptedException e) {
            LOGGER.error("Interrupted during warm-up phase; starting all listener threads.", e);
          }
          if (running) {
            LOGGER.info("Increasing max concurrent update consumers to operational value.");
            for (Map.Entry<Long, DefaultMessageListenerContainer> entry : jmsContainers.entrySet()) {
              entry.getValue().setMaxConcurrentConsumers(properties.getJms().getUpdate().getMaxConsumers());
            }
          }
        }

      }, "JmsContainer").start();

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
      ThreadPoolExecutor shutdownExecutor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), r -> {
        String threadName = "StopDaqUpdate";
        return new Thread(r, threadName);
      });
      Collection<ContainerShutdownTask> containerTasks = new ArrayList<ContainerShutdownTask>();
      for (Map.Entry<Long, DefaultMessageListenerContainer> entry : jmsContainers.entrySet()) {
        ContainerShutdownTask containerShutdownTask = new ContainerShutdownTask(entry.getValue());
        containerTasks.add(containerShutdownTask);
      }
      shutdownExecutor.invokeAll(containerTasks, 60, TimeUnit.SECONDS);
      shutdownExecutor.shutdown();
      jmsContainers.clear();
      daqThreadPoolTaskExecutor.shutdown();
//      LOGGER.info("Stopping JMS connections to DAQs");
//      updateConnectionFactory.stop(); //closes all JMS connections in the pool
    } catch (Exception e) {
      LOGGER.error("Exception caught while closing down the Spring listener/JMS thread pool", e);
    }
  }

  @Override
  public boolean isAutoStartup() {
    return true;
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
