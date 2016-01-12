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
package cern.c2mon.client.ext.history.data.event;

import java.sql.Timestamp;
import java.util.Collection;

import cern.c2mon.client.ext.history.common.id.HistoryUpdateId;
import cern.c2mon.client.ext.history.playback.data.HistoryStore;

/**
 * Used by {@link HistoryStore} to inform about events
 * 
 * @see HistoryStoreAdapter
 * 
 * @author vdeila
 */
public interface HistoryStoreListener {

  /**
   * Invoked when one or more objects have changed
   * 
   * @param historyUpdateIds
   *          The object ids which is changed
   */
  void onDataCollectionChanged(Collection<HistoryUpdateId> historyUpdateIds);

  /**
   * Invoked when one or more tags gets its first record in the store
   * 
   * @param historyUpdateIds
   *          the object ids which is initialized by having their first set of
   *          record(s)
   */
  void onDataInitialized(Collection<HistoryUpdateId> historyUpdateIds);

  /**
   * Invoked when new data has arrived and makes it possible get data from
   * further out in time, or if new data tags is added which makes the end time
   * move backwards to get the data for the new records.
   * 
   * @param newEndTime
   *          The new end time
   */
  void onPlaybackBufferIntervalUpdated(Timestamp newEndTime);

  /**
   * Invoked when all playback buffer is loaded
   */
  void onPlaybackBufferFullyLoaded();

}
