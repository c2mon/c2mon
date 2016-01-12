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
package cern.c2mon.client.ext.history.common.tag;

import java.util.Iterator;
import java.util.Set;

import cern.c2mon.client.common.util.ConcurrentIdentitySet;

/**
 * Used by the {@link HistoryTagLoadingManager} to keep track of what history data is
 * loaded.
 * 
 * @see HistoryTagLoadingManager
 * @see HistoryTagManagerListener
 * 
 * @author vdeila
 */
class HistoryTagConfigurationStatus {
  
  /** The status of the loading */
  private LoadingStatus status = LoadingStatus.NotInitialized;
  
  /** List of history tags */
  private final Set<HistoryTagManagerListener> subscribers = new ConcurrentIdentitySet<HistoryTagManagerListener>();
  
  /**
   * Creates an instance with an empty tag id list, and {@link #getStatus()} set
   * to {@link LoadingStatus#NotInitialized}
   */
  public HistoryTagConfigurationStatus() {
    
  }
  
  /**
   * @param oldStatus the current status
   * @param newStatus the new status to set if the current status equals <code>oldStatus</code>
   * @return <code>true</code> if the status were changed.
   */
  public synchronized boolean compareAndSetStatus(final LoadingStatus oldStatus, final LoadingStatus newStatus) {
    if (this.status == oldStatus) {
      this.status = newStatus;
      return true;
    }
    return false;
  }
  
  /**
   * @return the current loading status for the history configuration
   */
  public synchronized LoadingStatus getStatus() {
    return this.status;
  }
  
  /** 
   * @param listener the history tag to add
   */
  public void addSubscriber(final HistoryTagManagerListener listener) {
    this.subscribers.add(listener);
  }
  
  /** 
   * @param listener the history tag to remove
   */
  public void removeSubscriber(final HistoryTagManagerListener listener) {
    this.subscribers.remove(listener);
  }
  
  /**
   * @return an iterator for the history tags
   */
  public Iterator<HistoryTagManagerListener> getSubscribersIterator() {
    return this.subscribers.iterator();
  }
  
  /**
   * @return the number of history tags
   */
  public int getSubscribersCount() {
    return this.subscribers.size();
  }
  
  /** Loading statuses */
  public static enum LoadingStatus { 
    /** Not initialized */
    NotInitialized,
    /** Currently loading the data */
    Loading, 
    /** The data is ready to be read */
    Ready, 
    /** The data will not, or could not, be retrieved. */
    Invalid 
  }
}

