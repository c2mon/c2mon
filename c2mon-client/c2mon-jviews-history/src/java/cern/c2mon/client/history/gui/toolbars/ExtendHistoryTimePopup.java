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
package cern.c2mon.client.history.gui.toolbars;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.HistoryPlayer;
import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.Timespan;
import cern.c2mon.client.common.history.exception.HistoryPlayerNotActiveException;
import cern.c2mon.client.common.history.exception.IllegalTimespanException;
import cern.c2mon.client.core.C2monHistoryManager;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.history.gui.components.TimeSpanChooser;
import cern.c2mon.client.history.gui.components.event.TimeSpanChooserListener;

/**
 * Popup for the user to extend the time periode for the history playback
 * 
 * @author vdeila
 * 
 */
public class ExtendHistoryTimePopup {

  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(ExtendHistoryTimePopup.class);

  /** The date format used for the time span chooser */
  private static String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";

  /** The margins used */
  private static final int PANEL_MARGIN = 6;

  /** The panel with the components in it */
  private final JPanel panel;

  /** The clock label */
  private final JLabel clockLabel;

  /** Panel for selecting a timespan */
  private final TimeSpanChooser timeSpanChooser;

  /** The apply button */
  private final JButton applyButton;

  /** The reset button */
  private final JButton resetButton;

  /** The history manager */
  private final C2monHistoryManager historyManager;

  /**
   * 
   * @param owner
   *          the owner component
   */
  public ExtendHistoryTimePopup() {
     this.historyManager = C2monServiceGateway.getHistoryManager();

    this.clockLabel = new JLabel();
    this.timeSpanChooser = new TimeSpanChooser();
    this.timeSpanChooser.setDateFormatString(DATE_FORMAT);
    this.timeSpanChooser.addTimeSpanChooserListener(new TimeSpanChooserEvents());

    this.resetButton = new JButton("Reset");
    this.applyButton = new JButton("Apply");
    
    this.resetButton.setToolTipText("Resets the changes made to the dates currently in use");

    final JPanel buttonsPanel = new JPanel();
    buttonsPanel.add(this.applyButton);
    buttonsPanel.add(this.resetButton);

    final JPanel southPanel = new JPanel(new BorderLayout());
    southPanel.add(buttonsPanel, BorderLayout.EAST);

    panel = new JPanel(new BorderLayout(PANEL_MARGIN, PANEL_MARGIN));
    panel.add(this.clockLabel, BorderLayout.NORTH);
    panel.add(this.timeSpanChooser, BorderLayout.CENTER);
    panel.add(southPanel, BorderLayout.SOUTH);

    panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1), BorderFactory.createCompoundBorder(BorderFactory
        .createRaisedBevelBorder(), BorderFactory.createEmptyBorder(PANEL_MARGIN, PANEL_MARGIN, PANEL_MARGIN, PANEL_MARGIN))));
    
    this.resetButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        reset();
      }
    });
    this.applyButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        extendHistoryTime(timeSpanChooser.getSelectedTimeSpan());
      }
    });
  }
  
  /**
   * Applies the extended time changes
   * 
   * @param extendedTimespan the new timespan to extend to
   */
  private void extendHistoryTime(final Timespan extendedTimespan) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("Trying to extend to %s", extendedTimespan.toString()));
    }
    
    final List<String> errorMessages = new ArrayList<String>();
    
    HistoryPlayer historyPlayer;
    
    try {
      historyPlayer = historyManager.getHistoryPlayer();
    }
    catch (HistoryPlayerNotActiveException e) {
      historyPlayer = null;
      LOG.error("Failed to get the history player", e);
    }
    
    if (historyPlayer == null) {
      errorMessages.add("The history player is not available, see the log.");
    }
    else {
      final HistoryProvider historyProvider = historyPlayer.getHistoryProvider();
      
      if (historyProvider == null) {
        errorMessages.add(
            "Cannot extend the history because the current history provider could not be retrieved.");
      }
      else {
        final Timespan provderLimits = historyProvider.getDateLimits();
        if (provderLimits != null) {
          if (provderLimits.getStart() != null
              && provderLimits.getStart().compareTo(extendedTimespan.getStart()) > 0) {
            errorMessages.add("The suggested start time is not supported by the history provider");
          }
          if (provderLimits.getEnd() != null
              && provderLimits.getEnd().compareTo(extendedTimespan.getEnd()) < 0) {
            errorMessages.add("The suggested end time is not supported by the history provider");
          }
        }
      }
      
      if (errorMessages.size() > 0) {
        final StringBuilder message = new StringBuilder();
        
        if (errorMessages.size() > 1) {
          message.append(String.format("%d errors ", errorMessages.size()));
        }
        else {
          message.append("An error ");
        }
        message.append("occured while trying to apply the new time span for the history playback:");
        
        for (String error : errorMessages) {
          message.append("\n- ");
          message.append(error);
        }
        
        JOptionPane.showMessageDialog(null, 
            message.toString(), 
            "History Player Configurations", 
            JOptionPane.ERROR_MESSAGE);
      }
      else {
        if (extendedTimespan.getStart().equals(historyPlayer.getStart())
            && extendedTimespan.getEnd().compareTo(historyPlayer.getEnd()) >= 0) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Extending the playback by telling the history player");
          }
          try {
            historyPlayer.extendTimespan(extendedTimespan);
          }
          catch (IllegalTimespanException e) {
            LOG.error("Couldn't expand the history playback time frame", e);
            JOptionPane.showMessageDialog(null, 
                "Failed to extend the history playback time frame. Please see the log for details.", 
                "History Player Configurations", 
                JOptionPane.ERROR_MESSAGE);
          }
        }
        else {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Stopping history mode");
          }
          historyManager.stopHistoryPlayerMode();
          if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Starting history mode with new timespan (%s)", extendedTimespan));
          }
          historyManager.startHistoryPlayerMode(historyProvider, extendedTimespan);
          if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("History mode is restarted with a new timespan (%s)", extendedTimespan));
          }
        }
      }
    }
  }
  
  /** Events triggered when the user selects new dates */
  class TimeSpanChooserEvents implements TimeSpanChooserListener {
    @Override
    public void onSelectedStartDateChanged(final Date newStartDate) {
      refreshTimespanChangedState();
    }
    @Override
    public void onSelectedEndDateChanged(final Date newEndDate) {
      refreshTimespanChangedState();
    }
  }
  
  /**
   * Updates the states whether the dates are changed or not
   */
  private void refreshTimespanChangedState() {
    try {
      final HistoryPlayer historyPlayer = this.historyManager.getHistoryPlayer();
      
      final boolean isChanged = 
        !timeSpanChooser.getSelectedStartDate().equals(historyPlayer.getStart())
        || !timeSpanChooser.getSelectedEndDate().equals(historyPlayer.getEnd());
      
      setTimespanChanged(isChanged);
    }
    catch (HistoryPlayerNotActiveException e) {
      LOG.warn("Cannot get history playback configurations.", e);
    }
  }

  /**
   * @param isChanged
   *          <code>true</code> if there is anything to apply and to reset.
   *          <code>false</code> otherwise
   */
  private void setTimespanChanged(final boolean isChanged) {
    this.resetButton.setEnabled(isChanged);
    this.applyButton.setEnabled(isChanged);
    
    if (isChanged) {
      this.applyButton.setToolTipText("Applies the new dates");
    }
    else {
      this.applyButton.setToolTipText("There is nothing to apply");
    }
  }

  /**
   * @param owner
   *          the component that the location is relative to
   * @param location
   *          the location on screen where the popup should appear
   */
  public void show(final Component owner, final Point location) {
    reset();
    
    final JFrame frame = new JFrame();
    frame.setUndecorated(true);
    
    frame.setContentPane(panel);
    frame.setLocationRelativeTo(owner);
    frame.setLocation(location);
    frame.pack();
    frame.setVisible(true);
    
    frame.addWindowFocusListener(new WindowFocusListener() {
      @Override
      public void windowLostFocus(final WindowEvent e) {
        final Window focusWindow = e.getOppositeWindow();
        if (focusWindow == null
            || focusWindow.getOwner() != frame) {
          frame.setVisible(false);
        }
      }
      @Override
      public void windowGainedFocus(WindowEvent e) { }
    });
  }

  /**
   * Resets the time and date to what the history player is currently set to
   */
  private void reset() {
     try {
       final HistoryPlayer historyPlayer = this.historyManager.getHistoryPlayer();
       timeSpanChooser.setSelectedStartDate(historyPlayer.getStart());
       timeSpanChooser.setSelectedEndDate(historyPlayer.getEnd());
       final Timespan maximumTimespan = historyPlayer.getHistoryProvider().getDateLimits();
       if (maximumTimespan == null) {
         LOG.error("The timespan of the history provider is null");
         timeSpanChooser.setSelectableDateRange(new Date(0), new Date(System.currentTimeMillis()));
       }
       else {
//         timeSpanChooser.setSelectableStartDateRange(
//             maximumTimespan.getStart(), 
//             historyPlayer.getStart());
//         
//         timeSpanChooser.setSelectableEndDateRange(
//             historyPlayer.getEnd(), 
//             maximumTimespan.getEnd());
         
         if (maximumTimespan.getStart().compareTo(historyPlayer.getStart()) >= 0
             && maximumTimespan.getEnd().compareTo(historyPlayer.getEnd()) <= 0) {
           timeSpanChooser.setEnabled(false);
         }
         else {
           timeSpanChooser.setEnabled(true);
         }
         
         timeSpanChooser.setSelectableDateRange(maximumTimespan.getStart(), maximumTimespan.getEnd());
       }
       clockLabel.setText(
           TimHistoryPlayerToolBar.CLOCK_FORMATTER.format(
               new Date(historyPlayer.getPlaybackControl().getClockTime())));
     }
     catch (HistoryPlayerNotActiveException e) {
       LOG.error("Cannot get history playback configurations.", e);
     }

    setTimespanChanged(false);
  }

}
