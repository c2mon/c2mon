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
package cern.c2mon.client.history.gui;

import java.awt.Component;

import javax.swing.JToolBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.client.ext.history.C2monHistoryGateway;
import cern.c2mon.client.ext.history.common.HistoryPlayer;
import cern.c2mon.client.ext.history.common.HistoryProvider;
import cern.c2mon.client.ext.history.common.event.HistoryPlayerAdapter;
import cern.c2mon.client.ext.history.common.event.HistoryPlayerListener;
import cern.c2mon.client.ext.history.common.exception.HistoryPlayerNotActiveException;
import cern.c2mon.client.history.gui.dialogs.HistoryPlayerSwitchDialog;
import cern.c2mon.client.history.gui.dialogs.InitializingProgressDialog;
import cern.c2mon.client.history.gui.toolbars.TimHistoryPlayerToolBar;

/**
 * This class contains methods for easily managing the GUI for the history
 * 
 * @author vdeila
 */
public final class HistoryGuiProvider {

  /** Log4j logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(HistoryGuiProvider.class);

  /**
   * 
   * @return a toolbar with the history player components in it
   */
  public static JToolBar createTimHistoryPlayerToolBar() {
    return new TimHistoryPlayerToolBar();
  }

  /**
   * 
   * @param parent
   *          the parent of the dialog. The dialog will appear in the center of
   *          the parent.
   * @return a switch dialog, call {@link HistoryPlayerSwitchDialog#show()} to
   *         show it
   */
  public static HistoryPlayerSwitchDialog createHistoryPlayerSwitchDialog(final Component parent) {
    return new HistoryPlayerSwitchDialog(parent);
  }

  /**
   * Creates the initialization progress dialog, and registers it to
   * automatically show whenever the history is initializing. <br/>
   * <br/>
   * The history player does NOT need to be active to register this.<br/>
   * 
   * @param parent
   *          the parent of the dialog. The dialog will appear in the center of
   *          the parent
   */
  public static void registerInitializationDialog(final Component parent) {
    final HistoryPlayerListener listener = new HistoryPlayerAdapter() {
      private InitializingProgressDialog initializingProgressDialog = null;

      @Override
      public void onActivatedHistoryPlayer() {
        if (initializingProgressDialog != null) {
          // Already created
          return;
        }

        // Gets the history player
        HistoryPlayer historyPlayer;
        try {
          historyPlayer = C2monHistoryGateway.getHistoryManager().getHistoryPlayer();
        }
        catch (HistoryPlayerNotActiveException e1) {
          LOG.error("Not history player is available, no initialization dialog will be shown.", e1);
          return;
        }

        // Creates the initialization dialog
        initializingProgressDialog = new InitializingProgressDialog(parent);

        // Make it listen to history player events
        C2monHistoryGateway.getHistoryManager().getHistoryPlayerEvents().addHistoryPlayerListener(initializingProgressDialog.getHistoryPlayerEvents());

        // Make it listen to history provider events
        final HistoryProvider historyProvider = historyPlayer.getHistoryProvider();
        if (historyProvider != null) {
          historyPlayer.getHistoryProvider().addHistoryProviderListener(initializingProgressDialog.getHistoryProviderEvents());
        }
      }

      @Override
      public void onHistoryProviderChanged(final HistoryProvider historyProvider) {
        if (initializingProgressDialog != null) {
          // Make it listen to the new history provider's events
          historyProvider.addHistoryProviderListener(initializingProgressDialog.getHistoryProviderEvents());
        }
      }
    };

    if (C2monHistoryGateway.getHistoryManager().isHistoryModeEnabled()) {
      listener.onActivatedHistoryPlayer();
    }

    // Makes it listen to history player events
    C2monHistoryGateway.getHistoryManager().getHistoryPlayerEvents().addHistoryPlayerListener(listener);
  }

  /** hidden constructor */
  private HistoryGuiProvider() {}
}
