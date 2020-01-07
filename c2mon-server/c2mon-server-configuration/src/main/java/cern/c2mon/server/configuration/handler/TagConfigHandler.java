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
package cern.c2mon.server.configuration.handler;

import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.tag.Tag;

/**
 * Common interface of the ConfigHandlers that
 * manages Tag objects.
 * 
 * @author Mark Brightwell
 *
 * @param <T> cache object type
 */
public interface TagConfigHandler<T extends Tag> {

  /**
   * Adds this Rule to the list of Rules that
   * need evaluating when this tag changes.
   * 
   * @param tagId the Tag that needs to point to the rule
   * @param ruleId the rule that now needs evaluating
   * @throws CacheElementNotFoundException if the tag cannot be found in the cache
   */
  void addRuleToTag(Long tagId, Long ruleId);
  
  /**
   * Removes this Rule from the list of Rules
   * that need evaluating when this Tag changes. 
   * 
   * @param tagId the tag pointing to the rule
   * @param ruleId the rule that no longer needs evaluating
   * @throws CacheElementNotFoundException if the tag cannot be found in the cache
   */
  void removeRuleFromTag(Long tagId, Long ruleId);
  
  /**
   * Removes the Alarm from the list of alarms
   * attached to the Tag.
   * 
   * @param tagId the Tag id
   * @param alarmId the id of the alarm to remove
   * @throws CacheElementNotFoundException if the tag cannot be found in the cache
   */
  void removeAlarmFromTag(Long tagId, Long alarmId);

  /**
   * Adds the alarm to the list of alarms associated to this
   * tag (locks tag).
   * @param tagId the id of the tag
   * @param alarmId the id of the alarm
   * @throws CacheElementNotFoundException if the tag cannot be found in the cache
   */
  void addAlarmToTag(Long tagId, Long alarmId);

  
  
}
