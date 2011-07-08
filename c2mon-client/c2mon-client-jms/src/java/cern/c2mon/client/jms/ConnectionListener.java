package cern.c2mon.client.jms;


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
   */
  void onConnection();

  /**
   * Called when the JMS connection is lost.
   */
  void onDisconnection();

}
