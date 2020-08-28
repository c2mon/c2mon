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
package cern.c2mon.server.cache;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import cern.c2mon.server.cache.common.ConfigurableCacheFacade;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

/**
 * Methods implemented by all Tag facade beans.
 * 
 * @author Mark Brightwell
 *
 * @param <T> the object type in the cache
 */
public interface CommonTagFacade<T extends Tag> extends ConfigurableCacheFacade<T> {

  /**
   * Adds the rule to the list of rules that need evaluating when
   * this tag is updated.
   * @param tag the tag
   * @param ruleId id of the rule where this tag is used
   */
  void addDependentRuleToTag(final T tag, final Long ruleId);
  
  /**
   * Update all the alarms in the cache associated to this given Tag.
   * 
   * <p>A returned empty list of alarms indicates that the Alarm updates
   * have been filtered out and clients should not be informed of this
   * Tag/Alarm update; this should normally never happen as the tag 
   * update should be filtered out itself and not trigger any alarm
   * evaluations. 
   * 
   * @param tag evaluate alarms associated to this Tag  
   * @return a list of the evaluated Alarms (no longer in the cache); 
   *          the list is empty if the Alarm updates were filtered out
   */
  List<Alarm> evaluateAlarms(T tag);
  
  /**
   * Determines whether the managed cache contains an element with the specified id.
   * @param id The tag id to search for
   * @return true, if the key exists in the the given cache
   */
  boolean isInTagCache(Long id);

//TODO is this method too complicated below: maybe we never need to have more then one flag in the lists  
  /**
   * As opposed to the <code>invalidate()</code> methods, this method allows fine grained control of the quality of the 
   * datatag: in a single call, multiple quality flags can be added and/or removed and and new quality description
   * can optionally be given. 
   * 
   * <p>To be used for internal server invalidation/validations as only sets the cache timestamp!
   * 
   * <p><b>IMPORTANT:</b> this method should be used in preference to multiple calls the the invalidate() method since it results
   * in a SINGLE notification to the cache listeners (each call to invalidate() results in a new object been passed to all
   * module listeners).
   * 
   * <p>This method is (write-)synchronized on the DataTag object.
   * 
   * @param tagId unique id of the Tag
   * @param flagsToAdd added flags 
   * @param flagsToRemove removed flags
   * @param qualityDescriptions for flags that are set, will attempt to retrieve descriptions from this map
   * @param timestamp sets the cache timestamp
   * 
   */
  void setQuality(Long tagId, Collection<TagQualityStatus> flagsToAdd, Collection<TagQualityStatus> flagsToRemove, 
      Map<TagQualityStatus, String> qualityDescriptions, Timestamp timestamp);
  
//  /**
//   * Returns a collection of all the processes that are
//   * linked to this Tag hierarchy.
//   * 
//   * <p>(DataTags are attached to unique Process and Equipment, 
//   * while Rules can inherit from many).
//   * @param tag a Tag
//   * @return the collection of Process ids this tag is attached to
//   */
//  Collection<Long> getParentProcesses(T tag);
//  
//  /**
//   * Returns a collection of all the equipments that are
//   * linked to this Tag hierarchy.
//   * 
//   * <p>(DataTags are attached to unique Process and Equipment, 
//   * while Rules can inherit from many).
//   * @param tag a Tag
//   * @return the collection of Equipment ids this tag is attached to
//   */
//  Collection<Long> getParentEquipments(T tag);

  /**
   * Removes this rule from the list of those that need evaluating when
   * this tag is updated.
   * 
   * @param tag the tag used in the rule (directly, not via another rule)
   * @param ruleTagId the id of the rule
   */
  void removeDependentRuleFromTag(T tag, Long ruleTagId);
  
  /**
   * Return the Tag with associated evaluated Alarms corresponding
   * to the Tag value. A frozen copy is returned.
   * 
   * @param id the Tag id
   * @return Tag and Alarms, with corresponding values (no longer residing in cache)
   */
  TagWithAlarms getTagWithAlarms(Long id);

  /**
   * Given a tag, get it's alarms.
   *
   * @param tag The tag.
   * @return A list of alarms
   */
  List<Alarm> getAlarms(Tag tag);

  /**
   * Get a list of all tag IDs in the cache.
   *
   * @return list of Tag IDs
   */
  List<Long> getKeys();

  /**
   * Get a tag by ID.
   *
   * @param id the ID
   * @return the Tag
   */
  T getTag(Long id);
  
  /**
   * Returns a tag copy by ID.
   *
   * @param id the ID
   * @return the Tag
   */
  T getCopy(Long id);
}
