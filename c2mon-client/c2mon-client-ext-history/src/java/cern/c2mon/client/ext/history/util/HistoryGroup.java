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
package cern.c2mon.client.ext.history.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cern.c2mon.client.ext.history.common.HistoryUpdate;
import cern.c2mon.client.ext.history.common.id.HistoryUpdateId;
import cern.c2mon.client.ext.history.playback.data.HistoryStore;

/**
 * This class holds a list of {@link HistoryUpdate}s. Is used by
 * {@link HistoryStore} to keep history for one id (tag id or supervision event
 * id) in memory.
 * 
 * @author vdeila
 * 
 */
public class HistoryGroup {

  /**
   * The data id which is associated with the records
   */
  private HistoryUpdateId historyUpdateId;

  /**
   * The records of the id
   */
  private final List<HistoryUpdate> history;

  /**
   * Lock for <code>history</code>
   */
  private ReentrantReadWriteLock historyLock = new ReentrantReadWriteLock();

  /**
   * 
   * @param historyUpdateId
   *          The data id which the records will be associated with
   */
  public HistoryGroup(final HistoryUpdateId historyUpdateId) {
    this.historyUpdateId = historyUpdateId;
    this.history = new ArrayList<HistoryUpdate>();
  }

  /**
   * @return the history
   */
  public HistoryUpdate[] getHistory() {
    try {
      this.historyLock.readLock().lock();
      return this.history.toArray(new HistoryUpdate[0]);
    }
    finally {
      this.historyLock.readLock().unlock();
    }
  }

  /**
   * 
   * @param dataTagValue
   *          The record to add to the list
   */
  public void add(final HistoryUpdate dataTagValue) {
    try {
      this.historyLock.writeLock().lock();
      this.history.add(dataTagValue);
    }
    finally {
      this.historyLock.writeLock().unlock();
    }
  }

  /**
   * @return The id which is associated with the records
   */
  public HistoryUpdateId getTagId() {
    return this.historyUpdateId;
  }

  /**
   * Sorts the records by the given comparator
   * 
   * @param comparator
   *          The comparator to use to sort the list
   */
  public void sortHistory(final Comparator<HistoryUpdate> comparator) {
    try {
      this.historyLock.writeLock().lock();
      Collections.sort(this.history, comparator);
    }
    finally {
      this.historyLock.writeLock().unlock();
    }
  }

}
