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
package cern.c2mon.client.history.gui.dialogs;

import java.awt.Component;
import java.sql.Timestamp;

import javax.swing.JOptionPane;

import cern.c2mon.client.common.history.HistoryPlayerEvents;
import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.event.HistoryPlayerListener;
import cern.c2mon.client.common.history.event.HistoryProviderListener;
import cern.c2mon.client.history.gui.dialogs.generic.ProgressDialog;

/**
 * The progress dialog should be registered to receive events from
 * {@link HistoryPlayerListener} and {@link HistoryProviderListener}. It should
 * be registered to the {@link HistoryPlayerEvents}, and will then appear when
 * the history is initializing, and will disappear when it is finish
 * initializing.
 * 
 * @author vdeila
 * 
 */
public class InitializingProgressDialog implements HistoryPlayerListener, HistoryProviderListener {

  /** The title of the progress dialog when loading initial historical data */
  private static final String PROGRESS_TITLE = "Initializing historical data";

  /**
   * The progress text in the progress dialog when loading initial historical
   * data
   */
  private static final String PROGRESS_TEXT = "Please wait while History Player is initializing new data";

  /** Dialogs will use this as the parent component */
  private final Component parentComponent;

  /** The progress dialog which appear when initializing new data tags */
  private ProgressDialog initializingProgressDialog;

  /**
   * 
   * @param parentComponent
   *          The parent of the dialog
   */
  public InitializingProgressDialog(final Component parentComponent) {
    this.parentComponent = parentComponent;
    this.initializingProgressDialog = null;
  }

  /**
   * 
   * @return The initializingProgressDialog, creates it if it doesn't exists.
   */
  private ProgressDialog getInitializingProgressDialog() {
    if (this.initializingProgressDialog == null) {
      this.initializingProgressDialog = new ProgressDialog(PROGRESS_TITLE, PROGRESS_TEXT);
    }
    return this.initializingProgressDialog;
  }

  /*
   * HistoryLoaderListener implementation
   */

  @Override
  public void onStoppedLoadingDueToOutOfMemory() {
    JOptionPane.showMessageDialog(parentComponent, "TIM Viewer is almost out of memory, the loading of the history have therefore stopped.\n"
        + "Please close some views or choose a shorter time period if you want to load more of the history.\n"
        + "Restart the history mode to begin loading more.", "History player - Out Of Memory!", JOptionPane.WARNING_MESSAGE);
  }

  @Override
  public void onInitializingHistoryStarted() {
    // Shows the progress bar
    getInitializingProgressDialog().setProgress(null);
    getInitializingProgressDialog().setStatus(null);
    getInitializingProgressDialog().setParent(parentComponent);
    getInitializingProgressDialog().show();
  }

  @Override
  public void onInitializingHistoryProgressStatusChanged(final String progressMessage) {
    getInitializingProgressDialog().setStatus(progressMessage);
  }

  @Override
  public void onInitializingHistoryFinished() {
    // Hides the progress bar
    getInitializingProgressDialog().hide();
  }

  /*
   * HistoryProviderListener implementation
   */

  @Override
  public void queryProgressChanged(final double percent) {
    getInitializingProgressDialog().setProgress(percent);
  }

  @Override
  public void queryFinished() {
  }

  @Override
  public void queryStarting() {
  }

  @Override
  public void onActivatedHistoryPlayer() {
  }

  @Override
  public void onDeactivatingHistoryPlayer() {
  }

  @Override
  public void onHistoryIsFullyLoaded() {
  }

  @Override
  public void onHistoryDataAvailabilityChanged(Timestamp newTime) {
  }

  @Override
  public void onInitializingHistoryProgressChanged(double percent) {
  }

  @Override
  public void onHistoryProviderChanged(HistoryProvider historyProvider) {
  }

}
