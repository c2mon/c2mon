package cern.c2mon.shared.client.lifecycle;

/**
 * Events that are logged in the SERVER_LIFECYCLE_LOG table on server start
 * and stops.
 * 
 * @author Mark Brightwell
 *
 */
public enum LifecycleEventType {
  
  /**
   * Corresponds to server start.
   */
  START, 
  
  /**
   * Corresponds to server stop.
   */
  STOP 
}
