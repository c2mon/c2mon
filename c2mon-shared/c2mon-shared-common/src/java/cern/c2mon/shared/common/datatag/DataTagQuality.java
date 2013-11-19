/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2010 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.common.datatag;

import java.io.Serializable;
import java.util.Map;


/**
 * The <code>DataTagQuality</code> interface is used to represent the quality
 * attribute of a <code>Tag</code>.
 * The most important information a <code>DataTagQuality</code> object provides
 * is whether a tag's value is valid (<code>isValid()</code>) or not. In addition
 * to that, more fine grained information about the reason for invalidity (e.g. an
 * acquisition error, a range-check failure etc.) is available when calling
 * {@link #getInvalidQualityStates()}. All possible reasons for an invalidation
 * are defined in the <code>TagQualityStatus</code> enumeration.
 *
 * @author Matthias Braeger
 * @see TagQualityStatus
 */
 
public interface DataTagQuality extends Cloneable, Serializable {
  
  /**
   * @return A copy of the Map with contains all invalid <code>TagQualityStatus</code>
   * states being set. In case of a valid tag quality this method returns an
   * empty Map.
   */
  Map<TagQualityStatus, String> getInvalidQualityStates();

  
  /**
   * @return true if the quality object represents a "valid" state --> no error 
   * conditions apply.
   */
  boolean isValid();
  
  
  /**
   * Checks whether the given quality status is set or not
   * @param status The quality status to be checked
   * @return <code>true</code>, if the given quality status has been set
   */
  boolean isInvalidStatusSet(final TagQualityStatus status);

  
  /**
   * @return <code>true</code>, if the <code>UNINITIALISED</code> status is not set.
   */
  boolean isInitialised();
  
  
  /**
   * @return <code>true</code>, if the <code>UNDEFINED_TAG</code> status is not set.
   */
  boolean isExistingTag();
  

  /**
   * Call this method to determine whether the source of the tag is accessible
   * or not.
   * @return <code>true</code>, if one of the following states is set:
   * <li> {@link TagQualityStatus#PROCESS_DOWN}
   * <li> {@link TagQualityStatus#EQUIPMENT_DOWN}
   * <li> {@link TagQualityStatus#SUBEQUIPMENT_DOWN}
   * <li> {@link TagQualityStatus#INACCESSIBLE}
   * <li> {@link TagQualityStatus#SERVER_HEARTBEAT_EXPIRED}
   * <li> {@link TagQualityStatus#JMS_CONNECTION_DOWN}
   */
  boolean isAccessible();

  
  /**
   * Resets all the error conditions so that the tag quality
   * is <code>OK</code>; 
   */
  void validate();

  
  /**
   * If the quality is valid, this method returns "OK". In case of an invalid
   * tag quality status this method will return the description of the quality
   * state with the highest severity (zero == highest). In case there are
   * several quality states with the same (highest) severity, a string
   * concatenation of all status descriptions is returned.
   * @return the quality description 
   */
  String getDescription();
 

  /**
   * Removes the given <code>TagQualityStatus</code>, if ever set
   * @param statusToRemove The status to remove
   */
  void removeInvalidStatus(TagQualityStatus statusToRemove);
  
  
  /**
   * Adds an invalidation status to the quality object without any
   * description of the reason. Better use {@link #addInvalidStatus(TagQualityStatus, String)}
   * instead.
   * 
   * <p>Notice that any older description for this flag is removed.
   * 
   * @param statusToAdd The invalidation status that shall be added
   * @return <code>true</code>, if the quality status has successfully been added
   */
  boolean addInvalidStatus(TagQualityStatus statusToAdd);
  
  
  /**
   * Invalidate the quality object and adds the status to the current set 
   * of quality states.
   * 
   * <p>Even if no description is provided (null), any older description for this
   * flag is removed.
   * 
   * @param statusToAdd The invalidation status to be added
   * @param description free-text description of why the tag is invalidated; if null
   *                      description is set to an empty String
   * @return <code>true</code>, if the quality status has successfully been added
   */
  boolean addInvalidStatus(TagQualityStatus statusToAdd, String description);
  
  
  /**
   * Sets the invalidation status to the quality object but without any
   * description of the reason. Better use 
   * {@link #setInvalidStatus(TagQualityStatus, String)} instead. Please
   * notice that all previously added quality states are overwritten.
   * 
   * @param status The invalidation status that shall be added
   * @return <code>true</code>, if the quality status has successfully been set
   */
  boolean setInvalidStatus(TagQualityStatus status);
  
  
  /**
   * Sets a new invalidation status to the quality object and adds a
   * description of the reason. Please notice that all previously
   * added quality states are overwritten.
   * 
   * @param status The invalidation status that shall be added
   * @param description free-text description of why the tag is invalidated
   * @return <code>true</code>, if the quality status has successfully been set
   */
  boolean setInvalidStatus(TagQualityStatus status, String description);
  
  
  /**
   * This method replaces all older quality states by the given map.
   *  
   * @param qualityStates map of quality states and their description.
   */
  void setInvalidStates(Map<TagQualityStatus, String> qualityStates);
  
  
  /**
   * Needed for cache listeners, which always clone the object before
   * notifying the listener.
   * 
   * @return the clone
   * @throws CloneNotSupportedException if not implemented/supported so far
   */
  DataTagQuality clone() throws CloneNotSupportedException;


  /**
   * Checks if the passed status flag is set with the same description.
   * A null or empty string description are treated equally.
   * 
   * @param status check if this status is set
   * @param qualityDescription check if the description for this status is the same
   * @return true if status and description are the same
   */
  boolean isInvalidStatusSetWithSameDescription(TagQualityStatus status, String qualityDescription);
}
