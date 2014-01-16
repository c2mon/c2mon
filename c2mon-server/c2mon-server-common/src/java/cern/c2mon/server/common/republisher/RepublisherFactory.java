package cern.c2mon.server.common.republisher;

/**
 * Factory for creating a Republisher.
 * 
 * @author Mark Brightwell
 *
 */
public class RepublisherFactory {

  /**
   * Creates a Republisher for use by the past publisher. 
   * 
   * <p>Life-cycle needs managing externally using Lifecycle methods.
   * 
   * @param publisher
   * @param eventName the name of the event type, used for logging
   * @return a republisher for this publisher
   */
  public static <T extends Object> Republisher<T> createRepublisher(Publisher<T> publisher, String eventName) {
    return new RepublisherImpl<T>(publisher, eventName);
  }
  
  
  
}
