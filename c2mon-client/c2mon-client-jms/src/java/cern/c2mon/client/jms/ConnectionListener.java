package cern.c2mon.client.jms;


/**
 * Interface that needs implementing by classes wishing to be notified of JMS
 * connection/disconnection events. 
 * 
 * <p>Notice these are not notified on final shutdown of the client application.
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
