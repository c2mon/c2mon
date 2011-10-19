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

import java.awt.BorderLayout;
import java.awt.Component;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistoryProviderFactory;
import cern.c2mon.client.common.history.HistoryProviderType;
import cern.c2mon.client.common.history.SavedHistoryEvent;
import cern.c2mon.client.common.history.Timespan;
import cern.c2mon.client.common.history.exception.HistoryProviderException;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.history.gui.components.HistoryPlayerConfigPanel;
import cern.c2mon.client.history.gui.dialogs.generic.ProgressDialog;

/**
 * 
 * @author vdeila
 * 
 */
public class HistoryPlayerSwitchDialog {

  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(HistoryPlayerSwitchDialog.class);

  /** The margins for the controls */
  private static final int PANEL_MARGIN = 12;
  
  /** The parent component of the dialog */
  private final Component parent;
  
  /** The saved history events dialog */
  private SavedHistoryEventsDialog savedHistoryEventsDialog = null;
  
  /**
   * 
   * @param parent
   *          The parent component of the dialog. The dialog will be in the
   *          center of the parent. Can be null, but the position will then be
   *          random.
   */
  public HistoryPlayerSwitchDialog(final Component parent) {
    this.parent = parent;
  }

  /**
   * @return a dialog containing the saved history events
   * @throws HistoryProviderException
   *           if the events couldn't be retrieved
   */
  private synchronized SavedHistoryEventsDialog getSavedHistoryEventsDialog() throws HistoryProviderException {
    if (this.savedHistoryEventsDialog == null) {
      final ProgressDialog progress = new ProgressDialog(
          "Loading saved history events", 
          "Please wait while loading the saved history events list");
      try {
        progress.setProgress(null);
        progress.show();
        
        final Collection<SavedHistoryEvent> savedEvents = C2monServiceGateway.getHistoryManager().getHistoryProviderFactory().createSavedHistoryEventsProvider().getSavedHistoryEvents();
  
        final List<Object> events = new ArrayList<Object>();
        events.add("None");
        events.addAll(savedEvents);
        Collections.sort(events, new EventComparator());
        
        this.savedHistoryEventsDialog = new SavedHistoryEventsDialog(this.parent, events.toArray());
      }
      finally {
        progress.hide();
      }
    }
    return this.savedHistoryEventsDialog;
  }
  
  /**
   * Sorter for sorting a list of {@link SavedHistoryEvent}
   */
  class EventComparator implements Comparator<Object> {
    @Override
    public int compare(final Object o1, final Object o2) {
      if (o1 == o2) {
        return 0;
      }
      if (o1 == null) {
        return 1;
      }
      if (o2 == null) {
        return -1;
      }
      
      final SavedHistoryEvent event1;
      final SavedHistoryEvent event2;
      if (o1 instanceof SavedHistoryEvent) {
        event1 = (SavedHistoryEvent) o1;
      }
      else {
        event1 = null;
      }
      if (o2 instanceof SavedHistoryEvent) {
        event2 = (SavedHistoryEvent) o2;
      }
      else {
        event2 = null;
      }
      // Objects which is not a SavedHistoryEvent comes first
      if (event1 == null || event2 == null) {
        if (event1 == event2) {
          return 0;
        }
        if (event1 == null) {
          return -1;
        }
        return 1;
      }
      
      // Dates with null values goes last
      if (event1.getStartDate() == null || event2.getStartDate() == null) {
        if (event1.getStartDate() != event2.getStartDate()) {
          if (event1.getStartDate() == null) {
            return 1;
          }
          return -1;
        }
      }
      else {
        // Start date descending
        final int dateResult = event1.getStartDate().compareTo(event2.getStartDate());
        if (dateResult != 0) {
          return dateResult * -1;
        }
      }
      
      // By id descending
      if (event1.getId() < event2.getId()) {
        return 1;
      }
      else if (event1.getId() > event2.getId()) {
        return -1;
      }
      
      return 0;
    }
  }
  
  /**
   * 
   * @return <code>true</code> if the history player was successfully
   *         configured. The history player can then be activated.
   *         <code>false</code> if the history player is currently active, or if
   *         it couldn't configure the player.
   */
  public void show() {
    if (C2monServiceGateway.getHistoryManager().isHistoryModeEnabled()) {
      return;
    }
    else 
    {
      new Thread("History-Configuration-Window-Thread") {
        @Override
        public void run() {
          showConfiguration();
        }
      }.start();
    }
  }
  
  /**
   * Shows the configurations dialog
   */
  private void showConfiguration() {
    final HistoryPlayerConfigPanel historyPlayerConfigPanel = new HistoryPlayerConfigPanel();

    final JPanel switchPanel = new JPanel(new BorderLayout(PANEL_MARGIN, PANEL_MARGIN));
    final JLabel selectedEventLabel = new JLabel();
    selectedEventLabel.setVisible(false);
    
    Object savedEvent = "Saved events..";
    if (!C2monServiceGateway.getHistoryManager().getHistoryProviderAvailability().isAvailable(HistoryProviderType.SAVED_HISTORY_EVENTS)) {
      final JButton savedEventButton = new JButton(savedEvent.toString());
      savedEventButton.setEnabled(false);
      savedEventButton.setToolTipText("The history events is not accessible at the time. Check that the properties are set correctly.");
      savedEvent = savedEventButton;
    } 
    
    switchPanel.add(historyPlayerConfigPanel, BorderLayout.CENTER);
    switchPanel.add(selectedEventLabel, BorderLayout.SOUTH);
    
    final Object[] options = new Object[] { "Ok", "Cancel", savedEvent };
    
    SavedHistoryEvent savedHistoryEvent = null;
    
    Date start = null;
    Date end = null;
    while (start == null) {
      if (savedHistoryEvent == null) {
        selectedEventLabel.setVisible(false);
        historyPlayerConfigPanel.setChoosersEnabled(true);
        historyPlayerConfigPanel.resetDates();
      }
      else {
        selectedEventLabel.setText("Saved event: " + savedHistoryEvent.getName());
        selectedEventLabel.setVisible(true);
        historyPlayerConfigPanel.setChoosersEnabled(false);
        if (savedHistoryEvent.getStartDate() == null) {
          historyPlayerConfigPanel.setStartDate(new Timestamp(0));
        }
        else {
          historyPlayerConfigPanel.setStartDate(savedHistoryEvent.getStartDate());
        }
        if (savedHistoryEvent.getEndDate() == null) {
          historyPlayerConfigPanel.setEndDate(new Timestamp(0));
        }
        else {
          historyPlayerConfigPanel.setEndDate(savedHistoryEvent.getEndDate());
        }
      }
      
      final int result = JOptionPane.showOptionDialog(
          parent, 
          switchPanel, 
          "History Player Configuration", 
          JOptionPane.DEFAULT_OPTION, 
          JOptionPane.PLAIN_MESSAGE, 
          null, 
          options, 
          options[0]);

      switch (result) {
      case 0: // Ok
        // check time period
        start = historyPlayerConfigPanel.getStartDate();
        end = historyPlayerConfigPanel.getEndDate();
        long timePeriod = end.getTime() - start.getTime();
        if (timePeriod < 0) {
          JOptionPane.showMessageDialog(null, "The end date has to be after the start date!", "History player's time not valid", JOptionPane.ERROR_MESSAGE);
          break;
        }
        break;
      case 2: // Saved events
        try {
          final SavedHistoryEventsDialog dialog = getSavedHistoryEventsDialog();
          dialog.setSelectedObject(savedHistoryEvent);
          if (dialog.showDialog()) {
            savedHistoryEvent = dialog.getSelectedSavedHistoryEvent();
            if (savedHistoryEvent == null) {
              historyPlayerConfigPanel.resetDates();
            }
          }
        }
        catch (HistoryProviderException e) {
          LOG.error("Could not get a saved history events provider..", e);
          JOptionPane.showMessageDialog(
              parent, 
              "Couldn't fetch the list of saved history events. Please see the log for more details.", 
              "Failed to get list of events", 
              JOptionPane.ERROR_MESSAGE);
        }
        break;
      case 1: // Cancel
      case JOptionPane.CLOSED_OPTION:
      default:
        return;
      }
    }
    
    final HistoryProviderFactory historyProviderFactory = C2monServiceGateway.getHistoryManager().getHistoryProviderFactory();
    final HistoryProvider historyProvider;
    
    try {
      if (savedHistoryEvent == null) {
        historyProvider = historyProviderFactory.createHistoryProvider();
      }
      else {
        start = savedHistoryEvent.getStartDate();
        end = savedHistoryEvent.getEndDate();
        historyProvider = historyProviderFactory.createSavedHistoryProvider(savedHistoryEvent);
      }
    }
    catch (HistoryProviderException e) {
      LOG.error("Could not get a history provider..", e);
      JOptionPane.showMessageDialog(
          parent, 
          "Cannot start the history player because no provider is available. Please see the log for more details.", 
          "Failed to start history player", 
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    
    if (start == null || end == null) {
      LOG.info("The start or end date is not set, the history player can therefore not be started..");
      JOptionPane.showMessageDialog(
          parent, 
          "The start or end date is not set, the history player can therefore not be started..", 
          "Cannot start the history player", 
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    
    startHistoryPlayer(
        start, 
        end, 
        historyProvider);
  }
  
  /**
   * @param start
   *          the start time
   * @param end
   *          the end time
   * @param historyProviderType
   *          the history provider type to use
   */
  private void startHistoryPlayer(final Date start, final Date end, final HistoryProvider historyProvider) {
    final Timespan historyTimespan = new Timespan(start, end);
    
    final Thread startHistoryModeThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          C2monServiceGateway.getHistoryManager().startHistoryPlayerMode(historyProvider, historyTimespan);
        }
        catch (Exception e) {
          LOG.error("Something went wrong when starting the history player", e);
          if (C2monServiceGateway.getHistoryManager().isHistoryModeEnabled()) {
            C2monServiceGateway.getHistoryManager().stopHistoryPlayerMode();
          }
          JOptionPane.showMessageDialog(parent, "Cannot start the history player. Please see the log for more information.", "Can't start history player", JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    startHistoryModeThread.setName("Start-History-Mode-Thread");
    startHistoryModeThread.start();
  }
}
