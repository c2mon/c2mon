package cern.c2mon.client.jms;

/**
 * Service monitoring the health of JMS topic subscriptions.
 * Register a listener to be notified of this.
 * 
 * <p>Can only register a given listener once (duplicate registrations ignored).
 * 
 * @author Mark Brightwell
 *
 */
public interface JmsHealthMonitor {

  /**
   * Register a listener for callbacks on JMS subscription problems.
   * 
   * @param jmsHealthListener listener that will be called
   */
  void registerHealthListener(JmsHealthListener jmsHealthListener);

  /**
   * Unregister the listener.
   * @param jmsHealthListener listener
   */
  void removeHealthListener(JmsHealthListener jmsHealthListener);
}
