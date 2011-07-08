package cern.c2mon.client.jms;

import java.util.Collection;

/**
 * Interface that needs implementing by classes wishing to be notified of JMS
 * connection/disconnection events.
 * 
 * @author Mark Brightwell
 * 
 */
public interface ConnectionListener {

  /**
   * Called when the JMS connection is established.
   * 
   * @param registeredIds ids of tags for which a listener has
   *                        been registered
   */
  void onConnection(Collection<Long> registeredIds);

  /**
   * Called when the JMS connection is lost.
   */
  void onDisconnection();

}
