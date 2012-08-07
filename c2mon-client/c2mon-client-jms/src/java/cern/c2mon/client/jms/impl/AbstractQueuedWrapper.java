package cern.c2mon.client.jms.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
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
public abstract class AbstractQueuedWrapper<U> implements Lifecycle, MessageListener {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AbstractQueuedWrapper.class);
  
  /**
   * Queue of events waiting to be processed. If this is full, incoming thread will
   * be blocked and SubscriptionHealthMonitor will be notified.
   */
  private ArrayBlockingQueue<U> eventQueue;
  
  /**
   * Single listener for slow consumer callbacks.
   */
  private SlowConsumerListener slowConsumerListener;
  
  /**
   * Poll timeout.
   */
  private static final int POLL_TIMEOUT = 2;

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
  
  /**
   * Notifies the listeners of this event.
   * @param event the incoming event
   */
  protected abstract void notifyListeners(U event);
  
  public AbstractQueuedWrapper(final int queueCapacity, final SlowConsumerListener slowConsumerListener,
                                    final ExecutorService executorService) {    
    super();
    this.slowConsumerListener = slowConsumerListener;
    eventQueue = new ArrayBlockingQueue<U>(queueCapacity);    
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
          LOGGER.error("Exception caught while polling queue: " + e);
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
        if (LOGGER.isTraceEnabled())
           LOGGER.trace("AbstractQueuedWrapper received message for " + this.getClass().getSimpleName());
        
        U event = convertMessage(message);
        if (notificationTime.get() != 0 && (System.currentTimeMillis() - notificationTime.get()) > notificationTimeBeforeWarning.get()) {
          String warning = "Slow consumer warning: " + this.getClass().getSimpleName()
                              + " client application is not consuming updates correctly and should be restarted! " 
                              + " Info: " + getDescription(event);
          LOGGER.warn(warning);
          slowConsumerListener.onSlowConsumer(warning);
        }
        eventQueue.put(event);
      } else {
        LOGGER.warn("Non-text message received for " + this.getClass().getSimpleName() + " - ignoring event");
      }
    } catch (Exception e) {
      LOGGER.error("Exception caught while processing incoming server event with " + this.getClass().getSimpleName(), e);
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
    LOGGER.debug("Stopping listener thread");
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
