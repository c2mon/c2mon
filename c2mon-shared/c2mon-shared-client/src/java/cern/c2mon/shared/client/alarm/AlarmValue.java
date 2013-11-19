package cern.c2mon.shared.client.alarm;

import java.sql.Timestamp;

import cern.c2mon.shared.client.request.ClientRequestResult;

/**
 * This 
 *
 * @author Matthias Braeger
 */
public interface AlarmValue extends ClientRequestResult {

  /**
   * Get the alarm's unique identifier
   * @return the alarm's unique identifier
   */
  Long getId();
  
  /**
   * Get the UTC timestamp of the alarm's last state change
   * @return the UTC timestamp of the alarm's last state change
   */
  Timestamp getTimestamp();
  
  /**
   * Get the optional additional info on the alarm that is to be sent to
   * LASER as a "user-defined" fault state property.
   * @return the optional additional info on the alarm
   */
  String getInfo();
  
  /**
   * Get the alarm's LASER fault family
   * @return the alarm's LASER fault family
   */
  String getFaultFamily();
  
  /**
   * Get the alarm's LASER fault member
   * @return the alarm's LASER fault member
   */
  String getFaultMember();
  
  /**
   * Get the alarm's LASER fault code
   * @return the alarm's LASER fault code
   */
  int getFaultCode();
  
  /**
   * Get the unique identifier of the Tag to which the alarm is attached
   * @return the unique identifier of the Tag
   */
  Long getTagId();
  
  /** 
   * @return The description of the Tag 
   */
  String getTagDescription();
  
  /**
   * @return true if the alarm is currently active.
   */
  boolean isActive();
  
  /**
   * @return true if this.alarm is more recent.
   */
  boolean isMoreRecentThan(AlarmValue alarm);
  
  /**
   * @return A clone of the object
   * @throws CloneNotSupportedException Thrown, if a field is not clonable
   */
  AlarmValue clone() throws CloneNotSupportedException;
}
