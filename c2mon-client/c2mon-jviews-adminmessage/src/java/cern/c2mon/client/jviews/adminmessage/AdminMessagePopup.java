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
package cern.c2mon.client.jviews.adminmessage;

import java.awt.Component;

import javax.swing.JOptionPane;

import cern.c2mon.client.common.admin.AdminMessage;

/**
 * Utility class that contains a method for showing an {@link AdminMessage} to
 * the user
 * 
 * @author vdeila
 */
public class AdminMessagePopup {

  /**
   * Shows an administrator message popup dialog
   * 
   * @param parent
   *          the parent of the popup
   * @param adminMessage
   *          the admin message to show
   */
  public static void showAdminMessage(final Component parent, final AdminMessage adminMessage) {
    showAdminMessage(parent, adminMessage, "TIM Administrator message");
  }

  /**
   * Shows an administrator message popup dialog
   * 
   * @param parent
   *          the parent of the popup
   * @param adminMessage
   *          the admin message to show
   * @param title
   *          the title of the popup
   */
  public static void showAdminMessage(final Component parent, final AdminMessage adminMessage, final String title) {
    final int messageType;
    switch (adminMessage.getType()) {
    case WARN:
      messageType = JOptionPane.WARNING_MESSAGE;
      break;
    default:
      messageType = JOptionPane.INFORMATION_MESSAGE;
      break;
    }

    JOptionPane.showMessageDialog(parent, new AdminMessagePanel(adminMessage), title, messageType);
  }

  /** Utility class */
  private AdminMessagePopup() {
    // Do nothing
  }
}
