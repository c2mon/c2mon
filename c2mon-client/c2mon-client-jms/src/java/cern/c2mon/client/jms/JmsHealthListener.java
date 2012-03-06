package cern.c2mon.client.jms;

/**
 * Implement this interface to be notified about problems with the
 * processing of incoming updates on the JMS topics.
 * 
 * <p>In general, these notifications indicate a serious problem with
 * possible data loss, so the client should take some appropriate
 * action on receiving these callbacks (e.g. notify the user).
 * 
 * <p>Register with the SubscriptionHeathMonitor.
 * 
 * @author Mark Brightwell
 *
 */
public interface JmsHealthListener {

  /**
   * Called when one of the topic subscribers is slow.
   * 
   * @param diagnosticMessage a human-readable message for displaying
   */
  void slowConsumerDetected(String diagnosticMessage);
  
}
