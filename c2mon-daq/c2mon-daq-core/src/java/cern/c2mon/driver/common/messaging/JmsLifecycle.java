package cern.c2mon.driver.common.messaging;

/**
 * Lifecycle interface used by the DriverKernel
 * for stopping & starting sender/receiver classes.
 * 
 * @author Mark Brightwell
 *
 */
public interface JmsLifecycle {

  /**
   * Connect callback if necessary.
   */
  void connect();

  /**
   * Shutdown callback when the DAQ is shutting down.
   */
  void shutdown();
  
}
