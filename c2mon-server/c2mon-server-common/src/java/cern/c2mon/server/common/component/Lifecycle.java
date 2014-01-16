package cern.c2mon.server.common.component;

/**
 * Internal server component lifecycle interface,
 * for components that do not live in the Spring context.
 * 
 * @author Mark Brightwell
 *
 */
public interface Lifecycle {

  /**
   * Start the component.
   */
  void start();
  
  /**
   * Stop this component.
   */
  void stop();
  
  /**
   * Checks if this component is running.
   * 
   * @return true if the component is running
   */
  boolean isRunning();
  
}
