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
package cern.c2mon.server.alarm;

import java.util.List;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.tag.Tag;

/**
 * Interface that must be implemented by classes wishing
 * to received Tag updates together with all associated
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
  
}
