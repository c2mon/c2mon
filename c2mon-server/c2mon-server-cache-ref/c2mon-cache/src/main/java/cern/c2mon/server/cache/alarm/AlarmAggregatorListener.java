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
package cern.c2mon.server.cache.alarm;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.tag.Tag;

import java.util.List;

/**
 * Interface that must be implemented by classes wishing
 * to received Tag updates together with all associated
 * <b>evaluated</b> alarms.
 *
 * @author Mark Brightwell
 *
 */
@FunctionalInterface
public interface AlarmAggregatorListener {

  /**
   * Is called when a Tag update has been received, and associated
   * alarms have been evaluated.
   *
   * @param tag the updated Tag
   * @param alarms the new values of the associated alarms;
   *          this list is <b>null</b> if no alarms are associated to the tag
   */
  void notifyOnUpdate(Tag tag, List<Alarm> alarms);

}
