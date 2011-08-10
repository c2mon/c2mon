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
package cern.c2mon.client.history.playback;

import java.sql.Timestamp;
import java.util.Collection;

import cern.c2mon.client.common.history.HistoryPlayer;
import cern.c2mon.client.common.history.event.HistoryPlayerListener;
import cern.c2mon.client.common.history.event.HistoryProviderListener;
import cern.c2mon.client.common.history.id.HistoryUpdateId;
import cern.c2mon.client.history.playback.components.ListenersManager;
import cern.c2mon.client.history.playback.data.event.HistoryLoaderListener;
import cern.c2mon.client.history.playback.data.event.HistoryStoreListener;

/**
 * This class forwards events from the {@link HistoryLoaderListener},
 * {@link HistoryStoreListener} and {@link HistoryProviderListener} to the
 * {@link HistoryPlayerListener}s
 * 
 * @author vdeila
 * 
 */
public class HistoryPlayerEventsForwarder implements HistoryLoaderListener, HistoryStoreListener, HistoryProviderListener {

  /**
   * The manager which keeps track of the listeners, should be the same as the
   * {@link HistoryPlayer} instance have
   */
  private final ListenersManager<HistoryPlayerListener> historyPlayerListeners;

  /**
   * Whether or not the history player is initializing, used to decide whether
   * or not to forward messages from the {@link HistoryProviderListener}
   */
  private volatile boolean isInitializing;

  /**
   * 
   * @param historyPlayerListeners
   *          The listeners which will get the events
   */
  public HistoryPlayerEventsForwarder(final ListenersManager<HistoryPlayerListener> historyPlayerListeners) {
    this.historyPlayerListeners = historyPlayerListeners;
    this.isInitializing = false;
  }

  @Override
  public void onInitializingHistoryProgressStatusChanged(final String progressMessage) {
    for (HistoryPlayerListener listener : historyPlayerListeners.getAll()) {
      listener.onInitializingHistoryProgressStatusChanged(progressMessage);
    }
  }

  @Override
  public void onInitializingHistoryStarting() {
    this.isInitializing = true;
    for (HistoryPlayerListener listener : historyPlayerListeners.getAll()) {
      listener.onInitializingHistoryStarted();
    }
  }

  @Override
  public void onInitializingHistoryFinished() {
    this.isInitializing = false;
    for (HistoryPlayerListener listener : historyPlayerListeners.getAll()) {
      listener.onInitializingHistoryFinished();
    }
  }

  @Override
  public void onStoppedLoadingDueToOutOfMemory() {
    for (HistoryPlayerListener listener : historyPlayerListeners.getAll()) {
      listener.onStoppedLoadingDueToOutOfMemory();
    }
  }

  @Override
  public void onPlaybackBufferFullyLoaded() {
    for (HistoryPlayerListener listener : historyPlayerListeners.getAll()) {
      listener.onHistoryIsFullyLoaded();
    }
  }

  @Override
  public void onPlaybackBufferIntervalUpdated(final Timestamp newEndTime) {
    for (HistoryPlayerListener listener : historyPlayerListeners.getAll()) {
      listener.onHistoryDataAvailabilityChanged(newEndTime);
    }
  }
  
  @Override
  public void queryStarting() {
    if (this.isInitializing) {
      for (HistoryPlayerListener listener : historyPlayerListeners.getAll()) {
        listener.onInitializingHistoryProgressChanged(-1);
      }
    }
  }

  @Override
  public void queryProgressChanged(final double percent) {
    if (this.isInitializing) {
      for (HistoryPlayerListener listener : historyPlayerListeners.getAll()) {
        listener.onInitializingHistoryProgressChanged(percent);
      }
    }
  }

  @Override
  public void queryFinished() {
    if (this.isInitializing) {
      for (HistoryPlayerListener listener : historyPlayerListeners.getAll()) {
        listener.onInitializingHistoryProgressChanged(-1);
      }
    }
  }

  @Override
  public void onDataCollectionChanged(final Collection<HistoryUpdateId> historyUpdateIds) {
  }

  @Override
  public void onDataInitialized(final Collection<HistoryUpdateId> historyUpdateIds) {
  }

}
