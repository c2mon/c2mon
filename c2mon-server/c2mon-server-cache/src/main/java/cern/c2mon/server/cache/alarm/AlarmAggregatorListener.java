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

import java.util.List;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.tag.Tag;

/**
 * Interface that must be implemented by classes wishing
 * to received Tag and Supervision updates together with all associated
 * <b>evaluated</b> alarms.
 * 
 * @author Mark Brightwell
 *
 */
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
  
  /**
   * Call is triggered by a supervision change event (Process up/down, (Sub-)Equipment up/down),
   * which is changing the quality of the tag and adding/removing the '[?]' from the additional 
   * info field of the alarm. 
   * 
   * @param tag the Tag that is affected by the supervision event
   * @param alarms The alarms of corresponding tag; 
   *               this list is <b>null</b> if no alarms are associated to the tag
   */
  void notifyOnSupervisionChange(Tag tag, List<Alarm> alarms);
}
