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
package cern.c2mon.client.jviews.adminmessage;

import java.awt.Component;

import javax.swing.JOptionPane;

import cern.c2mon.client.common.admin.BroadcastMessage;

/**
 * Utility class that contains a method for showing an {@link AdminMessage} to
 * the user
 * 
 * @author vdeila
 */
public class BroadcastMessagePopup {

  /**
   * Shows an administrator message popup dialog
   * 
   * @param parent
   *          the parent of the popup
   * @param broadcastMessage
   *          the admin message to show
   */
  public static void showAdminMessage(final Component parent, final BroadcastMessage broadcastMessage) {
    showAdminMessage(parent, broadcastMessage, "TIM Administrator message");
  }

  /**
   * Shows an administrator message popup dialog
   * 
   * @param parent
   *          the parent of the popup
   * @param broadcastMessage
   *          the admin message to show
   * @param title
   *          the title of the popup
   */
  public static void showAdminMessage(final Component parent, final BroadcastMessage broadcastMessage, final String title) {
    final int messageType;
    switch (broadcastMessage.getType()) {
    case WARN:
      messageType = JOptionPane.WARNING_MESSAGE;
      break;
    default:
      messageType = JOptionPane.INFORMATION_MESSAGE;
      break;
    }

    JOptionPane.showMessageDialog(parent, new BroadcastMessagePanel(broadcastMessage), title, messageType);
  }

  /** Utility class */
  private BroadcastMessagePopup() {
    // Do nothing
  }
}
