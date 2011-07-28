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
import java.util.Date;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.Timespan;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.history.dbaccess.HistorySessionFactory;
import cern.c2mon.client.history.dbaccess.exceptions.HistoryException;
import cern.c2mon.client.history.gui.components.HistoryPlayerConfigPanel;

/**
 * 
 * @author vdeila
 * 
 */
public class HistoryPlayerSwitchDialog {

  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(HistoryPlayerSwitchDialog.class);

  /** The parent component of the dialog */
  private final Component parent;

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
   * 
   * @return <code>true</code> if the history player was successfully
   *         configured. The history player can then be activated.
   *         <code>false</code> if the history player is currently active, or if
   *         it couldn't configure the player.
   */
  public boolean show() {
    if (C2monServiceGateway.getHistoryManager().isHistoryModeEnabled()) {
      return false;
    }
    else {
      //
      // Configuring the history player
      //

      // Show history player configuration dialog in order to set start and end
      // time of the history player
      final HistoryPlayerConfigPanel historyPlayerConfigPanel = new HistoryPlayerConfigPanel();

      int answer;
      Date start = null;
      Date end = null;
      do {
        answer = JOptionPane.showConfirmDialog(parent, historyPlayerConfigPanel, "History Player Configuration", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

        if (answer == JOptionPane.OK_OPTION) {
          // check time period
          start = historyPlayerConfigPanel.getStartDate();
          end = historyPlayerConfigPanel.getEndDate();
          long timePeriod = end.getTime() - start.getTime();
          if (timePeriod < 0) {
            JOptionPane.showMessageDialog(null, "The end date has to be after the start date!", "History player's time not valid", JOptionPane.ERROR_MESSAGE);
            answer = -1;
          }
        }
      }
      while (answer == -1);

      if (answer == JOptionPane.OK_OPTION) {
        HistoryProvider historyProvider = null;
        try {
          historyProvider = HistorySessionFactory.getInstance().createHistoryProvider();
        }
        catch (HistoryException e) {
          LOG.error("Cannot create a history provider, can't load historical data", e);
        }
        if (historyProvider == null) {
          JOptionPane.showMessageDialog(null, "Cannot get data, as no history provider is available!", "Can't load history data", JOptionPane.ERROR_MESSAGE);
          return false;
        }

        C2monServiceGateway.getHistoryManager().startHistoryPlayerMode(historyProvider, new Timespan(start, end));
        
        return true;
      } else {
        return false;
      }
    }
  }
}
