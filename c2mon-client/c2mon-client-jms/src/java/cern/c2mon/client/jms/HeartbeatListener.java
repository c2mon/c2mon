package cern.c2mon.client.jms;

import cern.c2mon.shared.client.supervision.Heartbeat;

/**
 * Needs implementing by classes wishing to register with the JmsProxy
 * for server heartbeat messages.
 * 
 * @author Mark Brightwell
 *
 */
public interface HeartbeatListener {

  /**
   * Called on reception of a heartbeat message from the server.
   */
  void onHeartbeat(Heartbeat heartbeat);
    
}
