/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.common.tag;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagValueDictionary;
import cern.c2mon.shared.common.rule.RuleInputValue;

/**
 * The Tag interface is the common interface for all tag objects within the TIM system:
 * data, control and rule tags.
 * 
 * <p>It only provides read methods as in general this object should only be modified
 * by the cache modules (with the object residing in the cache).
 * 
 * @author Mark Brightwell
 *
 */
public interface Tag extends RuleInputValue, Cacheable {
  
  /** 
   * Get the unique numeric identifier 
   * @return returns the id
   **/
  Long getId();

  Object clone() throws CloneNotSupportedException;
  
  /** Get the (unique) tag name */
  String getName();

  /** Get a free-text description of the tag */
  String getDescription();  

  /**
   * Get the data type of the tag's current value.
   * The data type is the name of the Java class without "java.lang.", e.g.
   * "Integer", "Boolean", "String" etc.
   */
  String getDataType();
  
  /**
   * @return the JAPC address on which the data tag is published,
   *         or <code>null</code> if not.
   */
  String getJapcAddress();

  /** Get the mode of the tag.
   * Possible modes are
   * <UL>
   * <LI>MODE_OPERATIONAL</LI>
   * <LI>MODE_TEST</LI>
   * <LI>MODE_MAINTENANCE</LI>
   * </UL>
   */
  short getMode();

  /** Returns true if the mode is MODE_OPERATIONAL */
  boolean isInOperation();

  /** Returns true if the mode is MODE_MAINTENANCE */
  boolean isInMaintenance();

  /** Returns true if the mode is MODE_TEST */
  boolean isInTest();
  
  /**
   * The cache sets the mode to "UNCONFIGURED" when it first receives and unknown
   * datatag. A datatag in UNCONFIGURED mode will stay in UNCONFIGURED mode until
   * it is configured in the database.
   * @return true if running in UNCONFIGURED mode (i.e. not configured in DB).
   */
  boolean isInUnconfigured();

  /** Returns the tag's current value */
  Object getValue();

  /** Returns a textual description of the tag's current value, if available */
  String getValueDescription();

  /** 
   * Returns the tag's data quality
   * @return the quality object
   **/
  DataTagQuality getDataTagQuality();

  /** 
   * Returns the tag's unit, if available 
   * @return the unit as String
   **/
  String getUnit();
  
  /**
   * Name of the JMS topic on which the tag will be distributed to clients.
   * @return the topic as a String
   */
  String getTopic();
  
  /** Returns the tag's value dictionary */
  DataTagValueDictionary getValueDictionary();
  
  /** Returns true if the tag's value is valid */
  boolean isValid();

  /**
   * Returns true if the tag is a regular tag, not a "fake" tag created on a 
   * client's request.
   */
  boolean isExistingTag();

  /** Returns true if the tag's current value is the result of a simulation */
  boolean isSimulated();

  /**
   * Returns a collection containing the identifiers of all rules that need to
   * be evaluated when THIS tags changes. If this tag isn't used by any rule,
   * an empty collection is returned (never returns null).
   * @return the collection of Ids
   */
  Collection<Long> getRuleIds();
  
  /**
   * Returns an own copy of the list of rules that need evaluating when
   * this tag changes.
   * @return list of rule ids
   */
  Collection<Long> getCopyRuleIds();

  /**
   * The server cache timestamp indicates when the change message passed the server
   * @return The server cache timestamp
   */
  Timestamp getCacheTimestamp();
  
  /**
   * Current implementation returns the "lowest" level timestamp that
   * is set in this Tag object (from lowest: source, daq, cache timestamp).
   * <b>Should never return null.</b>
   * @return The "lowest" level timestamp (source, daq or cache timestamp).
   */
  Timestamp getTimestamp();

  /**
   * Returns the Equipments this Tag depends on (for rules
   * this includes all "parent" equipments). If the Tag has no
   * associated Equipment, the returned list will be empty.
   * Should never return a null list.
   * 
   * @return list of Equipment ids; can be empty; never null
   */
  Set<Long> getEquipmentIds();
  
  /**
   * Returns the Processes this Tag depends on (for rules
   * this includes all "parent" processes). If the Tag has no
   * associated Equipment, the returned list will be empty.
   * Should never return a null list.
   * 
   * @return list of Process ids; can be empty; never null
   */
  Set<Long> getProcessIds();
  
  /**
   * Returns the ids of the alarms set on this Tag.
   * Never returns null.
   * 
   * <p>Modifications to the Collection during reconfiguration
   * need locking on the Tag writeLock.
   * 
   * @return a collection of Ids
   */
  Collection<Long> getAlarmIds();
  
  /**
   * Returns an own copy of the list of alarm set on this Tag.
   * 
   * <p>Never returns null
   * @return copy of list of alarm ids
   */
  Collection<Long> getCopyAlarmIds();

  /**
   * Returns the optional address on which this tag is published to DIP.
   * Can be null.
   * @return the DIP address as String; can return null!
   */
  String getDipAddress();   
  
  /**
   * Returns true if the tag should be saved in the short-term-log.
   * @return true if needs logging
   */
  boolean isLogged();

  /**
   * Set the JMS topic on which this tag will be published 
   * @param topic topic name
   */
  void setTopic(String topic);
  
 
  
}
