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

import java.util.Collection;

import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.tag.Tag;

/**
 * CommonTagFacade implementation that can be used on any {@link Tag}
 * cache object (DataTag, ControlTag and RuleTag). It locates the correct
 * facade bean to call according to the object passed. 
 * @author Mark Brightwell
 *
 */
public interface TagFacadeGateway extends CommonTagFacade<Tag> {

  /**
   * Return a list of Tags where the tag name is matching the given regular expression,
   * with associated evaluated Alarms corresponding to the Tag value.
   * A frozen copy is returned.
   * 
   * @param regex Either a Tag name or a regular expression with wildcards ('*' and '?' are supported)
   * @return A list of Tags and Alarms, with corresponding values (no longer residing in cache)
   */
  Collection<TagWithAlarms> getTagsWithAlarms(String regex);

  /**
   * Return a list of Tags with the respective process id,
   * with associated evaluated Alarms corresponding to the Tag value.
   * A frozen copy is returned.
   *
   * @param processId Tag process id to match
   * @return A list of Tags and Alarms, with corresponding values (no longer residing in cache)
   */
  Collection<TagWithAlarms> getTagsWithAlarmsByProcessId(Long processId);

  /**
   * Return a list of Tags with the respective equipment id,
   * with associated evaluated Alarms corresponding to the Tag value.
   * A frozen copy is returned.
   *
   * @param equipmentId Tag equipment id to match
   * @return A list of Tags and Alarms, with corresponding values (no longer residing in cache)
   */
  Collection<TagWithAlarms> getTagsWithAlarmsByEquipmentId(Long equipmentId);

  /**
   * Return a list of Tags with the respective sub equipment id,
   * with associated evaluated Alarms corresponding to the Tag value.
   * A frozen copy is returned.
   *
   * @param subEquipmentId Tag sub equipment id to match
   * @return A list of Tags and Alarms, with corresponding values (no longer residing in cache)
   */
  Collection<TagWithAlarms> getTagsWithAlarmsBySubEquipmentId(Long subEquipmentId);

  /**
   * Determines whether one of the tag caches already contains
   * an element with the specified id (looks in rule, control
   * and tag cache in that order).
   * 
   * @param id the id to search for
   * @return true if the id corresponds to some tag
   * @see TagLocationService#isInTagCache(Long)
   */
  @Override
  boolean isInTagCache(Long id);
}
