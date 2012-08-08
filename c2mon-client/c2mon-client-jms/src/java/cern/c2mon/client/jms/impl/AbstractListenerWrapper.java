package cern.c2mon.client.jms.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

/**
 * Abstract message listener implementation to be registered to
 * a given JMS destination and publish the incoming event to a
 * collection of listeners.
 * 
 * <p>Implementers need to provide a conversion method for passing
 * from message to the event and a listener call method for calling
 * a given listener.
 * 
 * <p>Access to the hidden collection of listeners is synchronized and
 * so all calls to this class are thread safe (implementers should
 * check their implementations of convertMessage and invokeListener
 * are also).
 * 
 * <p>Checks for slow consumers and notified the registered SlowConsumerListener
 * every time this is detected (may get many callbacks). If the client recovers,
 * messages will not be lost here (but may be discarded on the broker).
 * 
 * <p>Details of the queuing functionality are implemented in the parent 
 * {@link AbstractQueuedWrapper}.
 * 
 * <p>The start and stop methods should be called to stop and start the queuing
 * thread.
 * 
 * @author Mark Brightwell
 *
 * @param <T> the listener interface that needs calling
 * @param <U> the type of event encoded in the message
 */
public abstract class AbstractListenerWrapper<T, U> extends AbstractQueuedWrapper<U> {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AbstractListenerWrapper.class);
  
  /**
   * Listeners registered for receiving events.  
   */
  private Collection<T> listeners = new ArrayList<T>();
  

  /**
   * Calls the listener for the provided event.
   * @param listener the listener to call
   * @param event the event that needs notifying
   */
  protected abstract void invokeListener(T listener, U event);
  
  /**
   * Return true if this event should be filtered out, in which
   * case no listeners will be notified. Called once per incoming event.
   * 
   * <p>Allows the listener to filter out on timestamp for instance.
   * 
   * @param event the incoming event
   */
  protected abstract boolean filterout(U event);
  
  /**
   * Constructor.
   * @param queueCapacity size of queue of events waiting to be processed (exceptions thrown if full)
   * @param slowConsumerListener listener that will be called when a slow consumer is detected (slow event consumption)
   */
  public AbstractListenerWrapper(final int queueCapacity, final SlowConsumerListener slowConsumerListener, final ExecutorService executorService) {
    super(queueCapacity, slowConsumerListener, executorService);
  }

  /**
   * Registers the listener for event notifications.
   * 
   * @param listener the listener to notify on update 
   */
  public synchronized void addListener(final T listener) {    
    listeners.add(listener);    
  }
  
  /**
   * Registers the listener for event notifications.
   * 
   * @param listener the listener to notify on update 
   */
  public synchronized int getListenerCount() {    
    return listeners.size();
  }
  
  /**
   * Unsubscribes the listener for update notifications.
   * 
   * @param listener to remove
   */
  public synchronized void removeListener(final T listener) {
    listeners.remove(listener);
  }

  /**
   * Notifies listeners
   * @param event event to notify
   */
  protected synchronized void notifyListeners(U event) {
    if (!filterout(event)) {
      for (T listener : listeners) {
        invokeListener(listener, event);
      }  
    }
  }
 
}
