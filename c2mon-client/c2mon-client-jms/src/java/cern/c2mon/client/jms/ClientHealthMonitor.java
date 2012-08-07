package cern.c2mon.client.jms;

/**
 * Service monitoring the health of the client application. A client application
 * should register for health-state callbacks via the {@link C2monSupervisionManager}.
 * 
 * <p>Can only register a given listener once (duplicate registrations ignored).
 * 
 * <p>Notice that listeners will only be notified of a problem once, since they
 * are expected to take immediate action.
 * 
 * @author Mark Brightwell
 *
 */
public interface ClientHealthMonitor {

  /**
   * Register a listener for health-state callbacks, in particular on slow client consumption
   * of incoming data.
   * 
   * @param clientHealthListener listener that will be called when problems are detected
   */
  void addHealthListener(ClientHealthListener clientHealthListener);

  /**
   * Unregister the listener.
   * 
   * @param clientHealthListener listener
   */
  void removeHealthListener(ClientHealthListener clientHealthListener);
}
