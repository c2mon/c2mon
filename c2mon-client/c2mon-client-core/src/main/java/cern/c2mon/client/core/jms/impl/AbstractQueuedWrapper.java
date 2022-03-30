/******************************************************************************
 * Copyright (C) 2010-2018 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.client.core.jms.impl;

import java.sql.Timestamp;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import cern.c2mon.client.core.jms.EnqueuingEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.Lifecycle;

/**
 * Implements common queuing of incoming JMS messages before calling
 * listeners on a separate thread. Notifies a health monitoring interface
 * if slow consumers are detected.
 *
 * <p>Use the lifecycle methods at application startup/shutdown.
 *
 * @author Mark Brightwell
 *
 * @param <U> the type of event encoded in the message
 *
 */
@Slf4j
public abstract class AbstractQueuedWrapper<U> implements Lifecycle, MessageListener {

  /**
   * Queue of events waiting to be processed. If this is full, incoming thread will
   * be blocked and SubscriptionHealthMonitor will be notified.
   */
  private ArrayBlockingQueue<U> eventQueue;

  /**
   * Single listener for slow consumer callbacks.
   */
  private SlowConsumerListener slowConsumerListener;

  private EnqueuingEventListener enqueuingEventListener;

  /**
   * Poll timeout.
   */
  private static final int POLL_TIMEOUT = 2;

  private static final float QUEUE_SIZE_THRESHOLD = 0.10f;

  /**
   * Time given for the notification to subscribed listeners before
   * notifying of a slow consumer!
   */
  private AtomicLong notificationTimeBeforeWarning = new AtomicLong(30000);

  /**
   * Shutdown request made.
   */
  private volatile boolean shutdownRequest = false;

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;


  /**
   * Last queue size percentage notified to the listeners
   */
  private volatile float lastQueueCapacityPercentageNotified = 0f;

  /**
   * THe current queueCapacity
   */
  private final int queueCapacity;

  /**
   * Time the last notification started. Will notify as slow if
   * takes over 30s to return (only checked on new message arrival).
   */
  private AtomicLong notificationTime = new AtomicLong(0);

  /**
   * Converts the JMS message into an event of the required type.
   * @param message the JMS message
   * @return the event
   * @throws JMSException if error in using the message
   */
  protected abstract U convertMessage(Message message) throws JMSException;

  /**
   * Return some human-readable version of an event. Used for logging
   * warnings.
   * @param event an event
   * @return a readable string
   */
  protected abstract String getDescription(U event);

  protected abstract String getQueueName();

  /**
   * Notifies the listeners of this event.
   * @param event the incoming event
   */
  protected abstract void notifyListeners(U event);

  public AbstractQueuedWrapper(final int queueCapacity, final SlowConsumerListener slowConsumerListener,
                               final EnqueuingEventListener enqueuingEventListener, final ExecutorService executorService) {
    super();
    this.slowConsumerListener = slowConsumerListener;
    this.enqueuingEventListener = enqueuingEventListener;
    this.queueCapacity = queueCapacity;
    eventQueue = new ArrayBlockingQueue<>(queueCapacity);
    //notice the slow consumer notification only works for a single listener thread here: if change would need a map U->notificationTime as field
    executorService.submit(new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        U event;
        try {
          event = eventQueue.poll(POLL_TIMEOUT, TimeUnit.SECONDS);
          if (event != null) {
            notificationTime.set(System.currentTimeMillis());
            notifyListeners(event);
            notificationTime.set(0);
          }
        } catch (Exception e) {
          log.error("Exception caught while polling queue: ", e);
        }
        if (!shutdownRequest) {
          executorService.submit(this);
        }
        return Boolean.TRUE;
      }
    });
  }

  /**
   * Converts message into SupervisionEvent and notifies registered listeners.
   *
   * <p>All exceptions are caught and logged (both exceptions in message conversion
   * and thrown by the listeners).
   */
  @Override
  public void onMessage(final Message message) {
    try {
      if (message instanceof TextMessage) {

        if (log.isTraceEnabled()) {
          log.trace("AbstractQueuedWrapper received message for {}: {}", this.getClass().getSimpleName(), ((TextMessage)message).getText());
        }

        U event = convertMessage(message);
        long lastNotificationTime = notificationTime.get();

        float currentQueueSizePercentage = (float) getQueueSize() /  (float) queueCapacity;
        if(queueSizeThresholdReached(currentQueueSizePercentage)){
          String warning = "New enqueuing event for " + getQueueName() + " queue. " +
                  "Queue capacity : " + queueCapacity + ". " +
                  "Current number of elements in the queue : " + getQueueSize() + ". " +
                  "Filling percentage: " + (currentQueueSizePercentage * 100) + "%";
          log.warn(warning);
          enqueuingEventListener.onEnqueuingEvent(warning);
        }

        if (lastNotificationTime != 0 && (System.currentTimeMillis() - lastNotificationTime) > notificationTimeBeforeWarning.get()) {
          String warning = "Slow consumer class: " + this.getClass().getSimpleName() + ". "
                              + "C2MON client is not consuming updates correctly and should be restarted! "
                              + " Event type: " + getDescription(event);
          log.warn(warning);
          log.warn("No returning call from listener since {}", new Timestamp(lastNotificationTime));
          slowConsumerListener.onSlowConsumer(warning);
        }
        eventQueue.put(event);
      } else {
        log.warn("Non-text message received for " + this.getClass().getSimpleName() + " - ignoring event");
      }
    } catch (Exception e) {
      log.error("Exception caught while processing incoming server event with " + this.getClass().getSimpleName(), e);
    }
  }


  private boolean queueSizeThresholdReached(float currentQueueSizePercentage){
    //true if the current queue size percentage increased or decreased 10% since the last sent percentage notification
    if(currentQueueSizePercentage >= lastQueueCapacityPercentageNotified + QUEUE_SIZE_THRESHOLD) {
      lastQueueCapacityPercentageNotified += QUEUE_SIZE_THRESHOLD;
      return true;
    }else
    if(currentQueueSizePercentage <= lastQueueCapacityPercentageNotified - QUEUE_SIZE_THRESHOLD){
      lastQueueCapacityPercentageNotified -= QUEUE_SIZE_THRESHOLD;
      return true;
    }else{
      return false;
    }
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  /**
   * Can only be started/stopped once.
   */
  @Override
  public void start() {
    running = true;
  }

  /**
   * Final shutdown.
   */
  @Override
  public void stop() {
    log.debug("Stopping listener thread");
    shutdownRequest = true;
    running = false;
  }

  /**
   * @return size of the internal queue of events
   */
  public int getQueueSize(){
    return eventQueue.size();
  }

  /**
   * Default is 30s.
   * @return the time given to all listeners to return on an update
   * notification, in milliseconds; after this time the client is warned of a slow consumer
   */
  public long getNotificationTimeBeforeWarning() {
    return notificationTimeBeforeWarning.get();
  }

  /**
   * @param notificationTimeBeforeWarning the time given to all listeners to return on an update
   * notification, in milliseconds
   */
  public void setNotificationTimeBeforeWarning(long notificationTimeBeforeWarning) {
    this.notificationTimeBeforeWarning.set(notificationTimeBeforeWarning);
  }

}
