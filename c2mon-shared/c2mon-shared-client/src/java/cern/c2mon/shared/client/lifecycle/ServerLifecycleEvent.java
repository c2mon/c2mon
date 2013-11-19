package cern.c2mon.shared.client.lifecycle;

import java.sql.Timestamp;

/**
 * Server start/stop event.
 * 
 * @author Mark Brightwell
 *
 */
public class ServerLifecycleEvent {
  
  /**
   * Time of event.
   */
  private Timestamp eventTime;
  
  /**
   * Name of server.
   */
  private String serverName;
  
  /**
   * Type of this event.
   */
  private LifecycleEventType eventType;

  /**
   * Default constructor.
   */
  public ServerLifecycleEvent() {
    super();
  }

  /**
   * Constructor.
   * @param eventTime time of event
   * @param serverName name of server
   * @param eventType type of event
   */
  public ServerLifecycleEvent(final Timestamp eventTime, final String serverName, final LifecycleEventType eventType) {
    super();
    this.eventTime = eventTime;
    this.serverName = serverName;
    this.eventType = eventType;
  }

  /**
   * @return the eventTime
   */
  public Timestamp getEventTime() {
    return eventTime;
  }

  /**
   * @param eventTime the eventTime to set
   */
  public void setEventTime(final Timestamp eventTime) {
    this.eventTime = eventTime;
  }

  /**
   * @return the serverName
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * @param serverName the serverName to set
   */
  public void setServerName(final String serverName) {
    this.serverName = serverName;
  }

  /**
   * @return the eventType
   */
  public LifecycleEventType getEventType() {
    return eventType;
  }

  /**
   * @param eventType the eventType to set
   */
  public void setEventType(final LifecycleEventType eventType) {
    this.eventType = eventType;
  }
  
}
