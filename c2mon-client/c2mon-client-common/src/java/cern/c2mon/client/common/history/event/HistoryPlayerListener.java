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
package cern.c2mon.client.common.history.event;

import java.sql.Timestamp;
import java.util.Collection;

import cern.c2mon.client.common.history.HistoryProvider;

/**
 * Is used by the {@link cern.c2mon.client.common.history.HistoryPlayer} to
 * inform about events
 * 
 * @author vdeila
 */
public interface HistoryPlayerListener {

  /**
   * Is invoked when the history player is activated
   */
  void onActivatedHistoryPlayer();

  /**
   * Is invoked when the history player is deactivating
   */
  void onDeactivatingHistoryPlayer();

  /**
   * Invoked when the buffered availability of the history data has changed.
   * Will also be called for the ending time. After the last call, the
   * {@link #onHistoryIsFullyLoaded()} will be called.
   * 
   * @param newTime
   *          The new end time of the buffer
   */
  void onHistoryDataAvailabilityChanged(Timestamp newTime);

  /**
   * Invoked when the full history time is loaded
   */
  void onHistoryIsFullyLoaded();

  /**
   * Invoked when starting to retrieve the initial data
   */
  void onInitializingHistoryStarted();
  
  /**
   * Invoked when one or more tags gets its first record
   * 
   * @param tagIds
   *          the tag ids which is initialized by having their first set of
   *          record(s)
   */
  void onTagsInitialized(Collection<Long> tagIds);

  /**
   * Invoked with the current status of the initialization
   * 
   * @param progressMessage
   *          A message describing the current actions taken
   */
  void onInitializingHistoryProgressStatusChanged(String progressMessage);

  /**
   * Invoked when the progress percentage is changed (or if it doesn't know
   * anymore)
   * 
   * @param percent
   *          <code>-1.0</code> for unknown, or between <code>0.0</code> -
   *          <code>1.0</code>.
   */
  void onInitializingHistoryProgressChanged(double percent);

  /**
   * Invoked when it have retrieved all initial data
   */
  void onInitializingHistoryFinished();

  /**
   * Invoked if memory resources is too low to continue the loading process.
   */
  void onStoppedLoadingDueToOutOfMemory();

  /**
   * Invoked when a new history provider is set
   * 
   * @param historyProvider the new provider which is set
   */
  void onHistoryProviderChanged(HistoryProvider historyProvider);
}
