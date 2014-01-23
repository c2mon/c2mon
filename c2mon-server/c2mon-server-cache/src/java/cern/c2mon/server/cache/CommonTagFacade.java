/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
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
   * Adds an invalid status to the Tag, together with an associated desription. If this status
   * is already set, it remains set and the description is overwritten with the new one (if none
   * is provided, the old description is simply removed).
   * 
   * <p>If the invalidation causes no changes, the cache object is not updated (see filterout method).
   * 
   * @param tagId id of the Tag
   * @param statusToAdd status flag to add
   * @param statusDescription description associated to this flag; leave as null if no description is required
   * @param timestamp time of the invalidation
   */
  void invalidate(Long tagId, TagQualityStatus statusToAdd, String statusDescription, Timestamp timestamp);
  
  /**
   * Adds the rule to the list of rules that need evaluating when
   * this tag is updated.
   * @param tag the tag
   * @param ruleId id of the rule where this tag is used
   */
  void addDependentRuleToTag(final T tag, final Long ruleId);
  
  /**
   * Adds the alarm to the list of alarms for this tag cache object.
   * 
   * @param tag the tag on which the alarm is set
   * @param alarmId the id of the alarm
   */
  void addAlarm(T tag, Long alarmId);
  
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
  
}
