package cern.c2mon.server.common.republisher;

import org.springframework.jms.JmsException;

/**
 * Should be implemented by a service wishing to make use
 * of the Republisher functionalities.
 * 
 * @author Mark Brightwell
 *
 * @param <T> type of event that is published
 */
public interface Publisher<T> {

  /**
   * (Re-)publish the event. The calling class should throw a runtime {@link JmsException}
   * if the publication fails and should be re-attempted. Any other exception
   * thrown will result in the removal of the event from the re-publication list.
   * 
   * <p>IMPORTANT: this method should usually NOT call the Republisher publicationFailed
   * method. Rather, if the publication fails, throw an exception as described above. If
   * this publish method returns successully, the event will be removed from the re-publication
   * list!
   */
  void publish(T event);
  
}
