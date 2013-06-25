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

