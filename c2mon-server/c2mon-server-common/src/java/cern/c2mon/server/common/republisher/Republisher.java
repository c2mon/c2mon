package cern.c2mon.server.common.republisher;

import cern.c2mon.server.common.component.Lifecycle;

/**
 * A Republisher can be used for managing re-publication of
 * any events in case of failure. 
 * 
 * <p> Instantiate one of these through the RepublisherFactory.
 * 
 * @author Mark Brightwell
 *
 * @param <T> the type of event the publisher publishes
 */
public interface Republisher<T> extends Lifecycle {

  /**
   * Call this method to indicate that the publication of this
   * event failed and should be re-attempted. 
   * @param event publication failed for this event
   */
  void publicationFailed(T event);

  /**
   * Override republication delay (default is 10s)
   * @param republicationDelay in milliseconds
   */
  void setRepublicationDelay(int republicationDelay);
  
  /**
   * @return returns the total number of failed publication attempts since the
   * application started.
   */
  long getNumberFailedPublications();
  
  /**
   * @return returns the current number of events waiting for re-publication
   */
  int getSizeUnpublishedList();
}
