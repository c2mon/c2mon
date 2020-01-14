/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.client.common.tag;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.rule.RuleInputValue;
import cern.c2mon.shared.rule.RuleExpression;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

/**
 * The <code>Tag</code> interface is the immutable interface
 * of a <code>Tag</code> object, as seen by a
 * <code>DataTagUpdateListener</code>. It only allows to get the different
 * fields from the <code>Tag</code> object.
 * @see Tag
 * @author Matthias Braeger
 *
 * @TODO: Remove ClientDataTagValue inheritence
 */
public interface Tag extends RuleInputValue {

  /**
   * Returns DataTagQuality object
   * @return the DataTagQuality object for this data tag.
   */
  DataTagQuality getDataTagQuality();

  /**
   * Returns the tag value
   * @return the tag value
   */
  @Override
  Object getValue();

  /**
   * @return the description of the current value
   */
  String getValueDescription();

  /**
   * Returns the unit of the value
   * @return The unit of the value
   */
  String getUnit();

  /**
   * Returns the current mode of the tag.
   * @return Returns either OPERATIONAL, TEST or MAINTENANCE
   */
  TagMode getMode();

  /**
   * @return <code>true</code>, if the tag value is currently simulated and not
   *         corresponding to a live event.
   */
  boolean isSimulated();

  /**
   * Returns the values of the registered alarms or an empty collection,
   * if no alarm is defined on that tag.
   * @return A shallow copy of the collection of registered alarms
   */
  Collection<AlarmValue> getAlarms();

  /**
   * @return A shallow copy of the list of alarms id's that are defined for that tag.
   */
  Collection<Long> getAlarmIds();

  /**
   * Returns the list of equipment id's to which the tag is linked to.
   * A data tag is by definition always attached to only one equipment,
   * but rules might have dependencies to several equipments.
   * @return A shallow copy of the list of equipment id's that are defined for that tag.
   */
  Collection<Long> getEquipmentIds();

  /**
   * Returns the list of sub equipment id's to which the tag is linked to.
   * A data tag is by definition always attached to only one equipment,
   * but rules might have dependencies to several equipments.
   * @return A shallow copy of the list of sub equipment id's that are defined for that tag.
   */
  Collection<Long> getSubEquipmentIds();

  /**
   * Returns the list of process id's to which the tag is linked to.
   * A data tag is by definition always attached to only one processes,
   * but rules might have dependencies to several processes.
   * @return A shallow copy of the list of process id's that are defined for that tag.
   */
  Collection<Long> getProcessIds();

  /**
   * @return <code>true</code>, if the value of this reference object was
   * computed with a rule. In case of a client rule the identifier of the
   * referenced object is always <code>-1</code>.
   * @see ClientDataTagValue#getId()
   */
  boolean isRuleResult();

  /**
   * @return The <code>RuleExpression</code> object or null, if the reference
   *         does not represent a rule. In case of a client rule it always returns
   *         the local client <code>RuleExpression</code>.
   */
  RuleExpression getRuleExpression();

  /**
   * Returns the description of the tag
   * @return the tag description
   */
  String getDescription();

  /**
   * Returns the tag source timestamp. In case the tag is uninitialized
   * it return the default time stamp which is from 1970.
   * @return the tag source timestamp. This value is never <code>null</code>.
   */
  Timestamp getTimestamp();

  /**
   * Returns the time when the data tag update
   * has passed the DAQ module. This value might be
   * interesting for reordering the incoming events
   * in case of race conditions.
   * @return the DAQ timestamp, or <code>null</code> in case that
   *         this tag update has not yet been initialized or has
   *         not passed the DAQ (e.g. in case of communication errors
   *         between server and DAQ layer).
   * @see #getTimestamp()
   */
  Timestamp getDaqTimestamp();

  /**
   * Returns the time when the data tag update
   * has passed the server. This value might be
   * interesting for reordering the incoming events
   * in case of race conditions.
   * @return the server timestamp, or null in case that
   *         this tag has not yet been initialized by
   *         the server.
   * @see #getTimestamp()
   */
  Timestamp getServerTimestamp();

  /**
   * Returns the tag name
   * @return the tag name
   */
  String getName();

  /**
   * Returns the tag type in the form of a java Class
   * @return the tag type in the form of a java Class, or <code>null</code>
   *         if no initial value has yet been received from the server
   */
  Class<?> getType();

  /**
   * Returns the hash code of the class type that is used by this
   * class instance. The return value is one of the specified
   * <code>TypeNumeric</code> constants.
   * @return The hash code of the class type as <code>enum</code>. Returns
   *         {@link TypeNumeric#TYPE_UNKNOWN}, if value is <code>null</code>
   *         or the value Object not one of the know raw value types.
   * @see TypeNumeric
   */
  TypeNumeric getTypeNumeric();

  /**
   * Returns the Meta Data from the Tag
   * @return all meta data as a Map
   */
  Map<String, Object> getMetadata();

  /**
   * @return  <code>true</code>, if tag represents an Alive Control tag
   */
  boolean isAliveTag();

  /**
   * @return <code>true</code>, if tag represents a CommFault-, Alive- or Status tag
   */
  boolean isControlTag();
}
