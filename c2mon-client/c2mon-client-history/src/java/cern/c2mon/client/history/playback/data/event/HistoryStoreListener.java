/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.history.playback.data.event;

import java.sql.Timestamp;
import java.util.Collection;

/**
 * Used by {@link HistoryStore} to inform about events
 * 
 * @see HistoryStoreAdapter
 * 
 * @author vdeila
 */
public interface HistoryStoreListener {

  /**
   * Invoked when one or more tag(s) is added
   * 
   * @param tagIds
   *          The tagIds which is added
   */
  void onTagsAdded(Collection<Long> tagIds);

  /**
   * Invoked when new data has arrived and makes it possible get data from
   * further out in time, or if new data tags is added which makes the end time
   * move backwards to get the data for the new records.
   * 
   * @param newEndTime The new end time
   */
  void onPlaybackBufferIntervalUpdated(Timestamp newEndTime);
  
  /**
   * Invoked when all playback buffer is loaded
   */
  void onPlaybackBufferFullyLoaded();

}
