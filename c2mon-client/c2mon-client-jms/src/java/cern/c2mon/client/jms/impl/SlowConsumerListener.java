package cern.c2mon.client.jms.impl;

/**
 * Gets called every time a slow consumer is detected.
 * May get many callbacks if client has problems.
 * 
 * @author Mark Brightwell
 *
 */
public interface SlowConsumerListener {

  /**
   * Callback when slow consumer detected.
   * @param details details about the consumer
   */
  void onSlowConsumer(String details);
  
}
