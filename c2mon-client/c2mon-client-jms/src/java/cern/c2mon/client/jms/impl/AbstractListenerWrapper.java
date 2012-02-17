package cern.c2mon.client.jms.impl;

import java.util.ArrayList;
import java.util.Collection;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

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
 * <p>Acces to the hidden collection of listeners is synchronized and
 * so all calls to this class are thread safe (implementers should
 * check their implementations of convertMessage and invokeListener
 * are also).
 * 
 * @author Mark Brightwell
 *
 * @param <T> the listener interface that needs calling
 * @param <U> the type of event encoded in the message
 */
public abstract class AbstractListenerWrapper<T, U> implements MessageListener {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AbstractListenerWrapper.class);
  
  /**
   * Listeners registered for receiving events.  
   */
  private Collection<T> listeners = new ArrayList<T>();
    
  
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
   * Converts the JMS message into an event of the required type. 
   * @param message the JMS message
   * @return the event
   * @throws JMSException if error in using the message
   */
  protected abstract U convertMessage(Message message) throws JMSException;
  
  /**
   * Calls the listener for the provided event.
   * @param listener the listener to call
   * @param event the event that needs notifying
   */
  protected abstract void invokeListener(T listener, U event);
  
  /**
   * Converts message into SupervisionEvent and notifies registered listeners.
   * 
   * <p>All exceptions are caught and logged (both exceptions in message conversion
   * and thrown by the listeners).
   */
  @Override
  public synchronized void onMessage(final Message message) {
    try {
      if (message instanceof TextMessage) {
        
        LOGGER.debug("AbstractListenerWrapper received message for " + this.getClass().getSimpleName());
        U event = convertMessage(message);     
        LOGGER.debug("AbstractListenerWrapper: there is " + listeners.size() + " listeners waiting to be notified!");
        for (T listener : listeners) {
          invokeListener(listener, event);
        }
      } else {
        LOGGER.warn("Non-text message received for " + this.getClass().getSimpleName() + " - ignoring event");
      }
    } catch (Exception e) {
      LOGGER.error("Exception caught while processing incoming server event with " + this.getClass().getSimpleName(), e);
    }
  }

}
