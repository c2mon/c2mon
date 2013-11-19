package cern.c2mon.shared.client.lifecycle;

import java.util.List;

/**
 * Mapper for accessing Server lifecycle events stored
 * in STL.
 * 
 * @author Mark Brightwell
 *
 */
public interface ServerLifecycleMapper {

  /**
   * Log this event in the server lifecycle STL.
   * 
   * @param serverLifecycleEvent event to log
   */
  void logEvent(ServerLifecycleEvent serverLifecycleEvent);
  
  /**
   * Retrieves all events for a given server, ordered by the time
   * of the event.
   * @param serverName the name of the server
   * @return an ordered list of events, as returned by the iterator
   */
  List<ServerLifecycleEvent> getEventsForServer(String serverName);
  
  /**
   * Delete all lifecycle events in the DB for this server.
   * Used in tests.
   * @param serverName name of the server
   */
  void deleteAllForServer(String serverName);
}
