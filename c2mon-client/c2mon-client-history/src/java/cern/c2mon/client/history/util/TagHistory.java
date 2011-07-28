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
package cern.c2mon.client.history.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cern.c2mon.client.history.playback.data.HistoryStore;
import cern.c2mon.shared.client.tag.TagValueUpdate;

/**
 * This class holds a list of {@link TagValueUpdate}s.
 * Is used by {@link HistoryStore} to keep tag history in memory.
 * 
 * @author vdeila
 *
 */
public class TagHistory {
  
  /**
   * The tag Id which is associated with the records
   */
  private long tagId;
  
  /**
   * The records of the tag
   */
  private final List<TagValueUpdate> history;
  
  /**
   * Lock for <code>history</code>
   */
  private ReentrantReadWriteLock historyLock = new ReentrantReadWriteLock();
  
  /**
   * 
   * @param tagId The tag Id which the records will be associated with 
   */
  public TagHistory(final long tagId) {
    this.tagId = tagId;
    this.history = new ArrayList<TagValueUpdate>();
  }
  
  /**
   * @return the history
   */
  public TagValueUpdate[] getHistory() {
    try {
      this.historyLock.readLock().lock();
      return this.history.toArray(new TagValueUpdate[0]);
    }
    finally {
      this.historyLock.readLock().unlock();
    }
  }
  
  /**
   * 
   * @param dataTagValue The record to add to the list
   */
  public void add(final TagValueUpdate dataTagValue) {
    try {
      this.historyLock.writeLock().lock();
      this.history.add(dataTagValue);
    }
    finally {
      this.historyLock.writeLock().unlock();
    }
  }

  /**
   * @return The tag Id which is associated with the records
   */
  public long getTagId() {
    return this.tagId;
  }

  /**
   * Sorts the records by the given comparator
   * 
   * @param comparator The comparator to use to sort the list
   */
  public void sortHistory(final Comparator<TagValueUpdate> comparator) {
    try {
      this.historyLock.writeLock().lock();
      Collections.sort(this.history, comparator);
    }
    finally {
      this.historyLock.writeLock().unlock();
    }
  }
  
}
