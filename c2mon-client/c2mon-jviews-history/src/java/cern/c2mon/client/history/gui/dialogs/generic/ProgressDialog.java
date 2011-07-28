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
package cern.c2mon.client.history.gui.dialogs.generic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Dialog.ModalityType;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * Shows a progress dialog with message, status, and a progress bar
 * 
 * @author vdeila
 * 
 */
public class ProgressDialog {

  /** How often the progress bar value is updated */
  private static final int PROGRESS_VALUE_UPDATE_INTERVAL_MS = 50;
  
  /** The margins for the progress dialog */
  private static final int PANEL_MARGIN = 4;

  /**
   * The start of the second progress text in the progress dialog when loading
   * initial historical data. Is put before the progress message coming from
   * HistoryLoader.
   */
  private static final String PROGRESS_STATUS_START = "- ";

  /** A dialog that blocks the TIM Viewer while initializing data from STL */
  private JDialog dialog;

  /** The message label */
  private JLabel messageLabel;

  /** The status label */
  private JLabel statusLabel;

  /** The progress bar when loading the initial history */
  private JProgressBar progressBar;
  
  /** The current value of the progress bar */
  private Double progressBarPercent = null;

  /**
   * @param dialogTitle
   *          The title of the dialog
   * @param message
   *          The message to set for the dialog
   */
  public ProgressDialog(final String dialogTitle, final String message) {
    // set up the progress indicator dialog
    progressBar = new JProgressBar();
    progressBar.setIndeterminate(true);
    progressBar.setMaximum(10000);
    progressBar.setMinimum(0);
    messageLabel = new JLabel(message);
    statusLabel = new JLabel(" ");

    this.dialog = new JDialog();
    final JPanel progressPanel = new JPanel();
    final JPanel mainProgressPanel = new JPanel();
    mainProgressPanel.setLayout(new BorderLayout(PANEL_MARGIN, PANEL_MARGIN));
    mainProgressPanel.setBorder(BorderFactory.createEmptyBorder(PANEL_MARGIN, PANEL_MARGIN, PANEL_MARGIN, PANEL_MARGIN));

    progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.PAGE_AXIS));
    progressPanel.add(messageLabel);
    progressPanel.add(Box.createRigidArea(new Dimension(1, PANEL_MARGIN)));
    progressPanel.add(statusLabel);
    progressPanel.add(Box.createRigidArea(new Dimension(1, PANEL_MARGIN)));
    progressPanel.add(progressBar);

    mainProgressPanel.add(progressPanel);

    this.dialog.setTitle(dialogTitle);
    this.dialog.setModalityType(ModalityType.APPLICATION_MODAL);
    this.dialog.getContentPane().add(mainProgressPanel);
    this.dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    this.dialog.setResizable(false);
    this.dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    this.dialog.pack();
    //this.dialog.setLocation(500, 500);
    this.dialog.setLocationRelativeTo(null);
  }

  /**
   * 
   * @param parent
   *          the parent to set. Will affect the position of the dialog
   */
  public synchronized void setParent(final Component parent) {
    this.dialog.setLocationRelativeTo(parent);
  }
  
  /**
   * Shows the dialog
   */
  public synchronized void show() {
    if (dialog.isVisible()) {
      return;
    }
    final Thread progressThread = new Thread(new Runnable() {
      public void run() {
        startProgressBarUpdateThread();
        
        dialog.setVisible(true);
      }
    });
    progressThread.setName("TIM-UI-Progress-Thread");
    progressThread.start();
  }

  /**
   * Hides the dialog
   */
  public synchronized void hide() {
    dialog.setVisible(false);
    progressBarUpdateThreadRun.set(false);
  }

  /**
   * 
   * @param percent
   *          The percent to have in the progress bar, between 0.0 and 1.0,
   *          <code>null</code> to set it to intermediate
   */
  public synchronized void setProgress(final Double percent) {
    this.progressBarPercent = percent;
  }
  
  /**
   * false if the progress bar update thread should stop
   */
  private AtomicBoolean progressBarUpdateThreadRun = new AtomicBoolean();
  
  /**
   * Starts the progress bar update thread
   */
  private void startProgressBarUpdateThread() {
    progressBarUpdateThreadRun.set(true);
    final Thread thread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        while (progressBarUpdateThreadRun.get()) {
          final Double percent = progressBarPercent;
          if (percent == null) {
            progressBar.setIndeterminate(true);
          }
          else {
            progressBar.setIndeterminate(false);
            progressBar.setValue((int) (percent * progressBar.getMaximum()));
          }
          try {
            Thread.sleep(PROGRESS_VALUE_UPDATE_INTERVAL_MS);
          }
          catch (InterruptedException e) { }
        }
      }
    });
    thread.setName("Progress-bar-update-Thread");
    thread.start();
  }

  /**
   * 
   * @param text The message to set
   */
  public synchronized void setMessage(final String text) {
    messageLabel.setText(text);
  }

  /**
   * 
   * @param status The status to set
   */
  public synchronized void setStatus(final String status) {
    if (status == null || status.length() == 0) {
      statusLabel.setText(" ");
    }
    else {
      statusLabel.setText(PROGRESS_STATUS_START + status);
    }
  }

}
